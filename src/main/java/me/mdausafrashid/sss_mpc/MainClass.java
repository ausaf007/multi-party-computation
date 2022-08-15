package me.mdausafrashid.sss_mpc;

import com.codahale.shamir.Scheme;
import org.web3j.crypto.*;
import org.web3j.crypto.Sign.SignatureData;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

import static me.mdausafrashid.sss_mpc.Constants.PATH;
import static me.mdausafrashid.sss_mpc.Constants.RADIX;

public class MainClass {
    private static Bip39Wallet generate(String walletPassword) throws CipherException, IOException {
        File walletDirectory = new File(PATH);
        Bip39Wallet walletName = WalletUtils.generateBip39Wallet(walletPassword, walletDirectory);
        return walletName;
    }

    public static void main(String[] args) throws IOException, CipherException, InterruptedException, ExecutionException {
        Bip39Wallet mainWallet = null;
        Map<Integer, byte[]> mainMap = new HashMap<>();
        Map<Integer, byte[]> constructedMap = new HashMap<>();

        Scheme mainScheme = null;

        String publicKey = null;
        SignatureData signature = null;
        String message = null;

        while (true) {
            Scanner sc = new Scanner(System.in);

            System.out.print("(MAIN) MPC>>");
            String str = sc.nextLine();
            str = str.trim().toLowerCase();
            if (str.equals("quit") || str.equals("q") || str.equals("exit")) {
                break;
            } else if (str.equals("generate eth wallet")) {

                System.out.print("Enter Password for Wallet:");
                String walletPassword = sc.nextLine();
                walletPassword = walletPassword.trim();
                mainWallet = generate(walletPassword);
                System.out.println("Ethereum Wallet Generated Successfully!");

                Credentials credentials = WalletUtils.loadBip39Credentials(walletPassword, mainWallet.getMnemonic());
                String privateKey = credentials.getEcKeyPair().getPrivateKey().toString(RADIX);

                publicKey = credentials.getEcKeyPair().getPublicKey().toString(RADIX);

                System.out.println("Main Private Key: " + privateKey);
                System.out.println("Initiating Private Key Splitting");
                System.out.println("n: Number of shares");
                System.out.println("k: Threshold for signing");
                System.out.println("Enter n,k =");
                String nkString = sc.nextLine();
                String[] nkStringArr = nkString.split(",");
                int n = 0, k = 0;
                for (int i = 0; i < 2; i++) {
                    if (i == 0) {
                        n = Integer.parseInt(nkStringArr[i]);
                    } else {
                        k = Integer.parseInt(nkStringArr[i]);
                    }
                }
                mainScheme = new Scheme(new SecureRandom(), n, k);
                mainMap = mainScheme.split(privateKey.getBytes(StandardCharsets.UTF_8));

                // Displaying all the shares
                System.out.println("Private Keys for all shares:");
                for (int i = 1; i <= n; i++) {
                    System.out.println("for share=" + i + ":" + Base64.getEncoder().encodeToString(mainMap.get(i)));
                }
                System.out.println("Minimum Threshold(k) = " + k);

            } else if (str.equals("help") || str.equals("man")) {
                // TBD Manual
            } else if (str.startsWith("sign")) {
                if (mainWallet == null) {
                    // No eth wallet found. Requesting user to generate eth wallet first.
                    System.out.println("Please generate eth wallet first!");
                    continue;
                }
                str = str.substring(5);
                String[] shareArr = str.split(","); // Creating array of string
                if (shareArr.length < mainScheme.k()) {
                    System.out.println("Not enough users signed!");
                    continue;
                }
                for (int i = 1; i <= shareArr.length; i++) {
                    constructedMap.put(i, mainMap.get(i));
                }

                byte[] recoveredSecret = mainScheme.join(constructedMap);

                // Beginning Signing process
                System.out.println("Key Recovery Successful!");
                System.out.println("Private Key=" + new String(recoveredSecret, StandardCharsets.UTF_8));
                String recoveredSecretString = new String(recoveredSecret, StandardCharsets.UTF_8);
                System.out.println("Enter signing message =");
                message = sc.nextLine();

                ECKeyPair pair = new ECKeyPair(new BigInteger(recoveredSecretString, RADIX), new BigInteger(publicKey, RADIX));
                signature = Sign.signMessage(message.getBytes(StandardCharsets.UTF_8), pair);

            } else if (str.equals("validate")) {
                // Beginning Signature Verification Process
                try {
                    String pubKey = Sign.signedMessageToKey(message.getBytes(StandardCharsets.UTF_8), signature).toString(RADIX);
                    System.out.println("PubKey=" + pubKey);
                    System.out.println("publicKey=" + publicKey);
                    if (pubKey.equals(publicKey)) {
                        System.out.println("Signature Verification Successful!");
                    }
                } catch (SignatureException e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("Command not found. Try running 'help' to learn more.");
            }
        }
    }
}
