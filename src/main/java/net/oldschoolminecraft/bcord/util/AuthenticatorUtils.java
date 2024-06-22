package net.oldschoolminecraft.bcord.util;

import com.spire.barcode.BarCodeGenerator;
import com.spire.barcode.BarCodeType;
import com.spire.barcode.BarcodeSettings;
import com.spire.barcode.QRCodeECL;

import java.awt.image.BufferedImage;
import java.security.SecureRandom;

public class AuthenticatorUtils
{
    private static final SecureRandom rng = new SecureRandom();

    public static BufferedImage getQR(String username)
    {
        BarcodeSettings settings = new BarcodeSettings();
        settings.setType(BarCodeType.QR_Code);
        String data = getOTPLink("OSM", username, generateSecret());
        settings.setData(data);
        settings.setX(2);
        settings.setQRCodeECL(QRCodeECL.M);
        settings.setTopText(username);
        settings.setBottomText("OSM 2FA");
        settings.setShowText(false);
        settings.setShowTopText(true);
        settings.setShowBottomText(true);
        settings.hasBorder(false);
        BarCodeGenerator barCodeGenerator = new BarCodeGenerator(settings);
        return barCodeGenerator.generateImage();
    }

    private static String getOTPLink(String label, String user, String secret)
    {
        return String.format("otpauth://totp/%s:%s?secret=%s&issuer=%s", label, user, secret, label);
    }

    public static String generateSecret()
    {
        return generateString("ABCDEFGHIJGLMNOPQRSTUVWXYZ0123456789", 16);
    }

    private static String generateString(String characters, int length)
    {
        char[] text = new char[length];
        for (int i = 0; i < length; i++) text[i] = characters.charAt(rng.nextInt(characters.length()));
        return new String(text);
    }
}
