package org.formsapi.injector;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.formsapi.FormsAPIExtension;
import org.formsapi.forms.FormBuilder;
import org.formsapi.protocol.FormRequest;
import org.formsapi.protocol.FormResponse;
import org.geysermc.cumulus.form.Form;
import org.geysermc.geyser.session.GeyserSession;
import org.geysermc.geyser.translator.protocol.PacketTranslator;
import org.geysermc.geyser.translator.protocol.Translator;
import org.geysermc.mcprotocollib.protocol.packet.common.clientbound.ClientboundCustomPayloadPacket;

import java.nio.charset.StandardCharsets;

/**
 * Intercepts plugin messages from the Spigot server on the formsapi:request channel.
 */
@Translator(packet = ClientboundCustomPayloadPacket.class)
public class CustomPayloadInjector extends PacketTranslator<ClientboundCustomPayloadPacket> {

    public static final String CHANNEL_REQUEST = "formsapi:request";

    private final Gson gson = new GsonBuilder().create();

    @Override
    public void translate(GeyserSession session, ClientboundCustomPayloadPacket packet) {
        String channel = packet.getChannel().asString();

        if (CHANNEL_REQUEST.equals(channel)) {
            byte[] data = packet.getData();
            handleFormRequest(session, data);
        }
        // Note: Other plugin messages are handled by Geyser's default translator
    }

    private void handleFormRequest(GeyserSession session, byte[] data) {
        FormsAPIExtension extension = FormsAPIExtension.getInstance();
        if (extension == null) {
            return;
        }

        try {
            String json = new String(data, StandardCharsets.UTF_8);
            extension.logger().info("Received form request: " + json);

            FormRequest request = gson.fromJson(json, FormRequest.class);

            // Build and send the form
            Form form = FormBuilder.buildForm(request, response -> {
                // Log the response (can't send back via plugin messaging easily)
                extension.logger().info("Form response: " + gson.toJson(response));

                // Execute command based on response if it's a TPA form
                if (response.isSuccess() && "modal".equals(request.getFormType())) {
                    if (response.isConfirmed()) {
                        // Player clicked Accept (button1)
                        session.sendCommand("tpaccept");
                        extension.logger().info("Player accepted TPA request");
                    } else {
                        // Player clicked Deny (button2)
                        session.sendCommand("tpdeny");
                        extension.logger().info("Player denied TPA request");
                    }
                }
            }, session);

            session.sendForm(form);
            extension.logger().info("Sent form to player: " + session.javaUuid());

        } catch (Exception e) {
            extension.logger().warning("Failed to process form request: " + e.getMessage());
        }
    }
}
