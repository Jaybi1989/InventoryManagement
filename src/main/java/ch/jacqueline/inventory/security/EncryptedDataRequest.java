package ch.jacqueline.inventory.security;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter

public class EncryptedDataRequest {
    private String data;
    private String mac;

    @Override
    public String toString() {
        return "Encr5yptedDataRequest{" +
                "data='" + data.substring(0, Math.min(data.length(), 20)) + "..." + '\'' +
                ", mac='" + mac.substring(0, Math.min(mac.length(), 20)) + "..." + '\'' +
                '}';
    }
}
