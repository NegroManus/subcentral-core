package de.subcentral.mig;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class HTTPS {
    public static void main(String[] args) throws IOException {
        URL url = new URL(args[0]);
        URLConnection conn = url.openConnection();
        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        int c = 0;

        while ((c = rd.read()) != -1)
            System.out.print((char) c);
    }
}
