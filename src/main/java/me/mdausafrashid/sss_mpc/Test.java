package me.mdausafrashid.sss_mpc;

import java.util.Scanner;

public class Test {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter n,k =");
        String nkString = sc.nextLine();
        String nkStringArr[] = nkString.split(",");
        int n = 0, k = 0;
        for (int i = 0; i < 2; i++) {
            if (i == 0) {
                n = Integer.valueOf(nkStringArr[i]);
            } else {
                k = Integer.valueOf(nkStringArr[i]);
            }
        }
        System.out.println("n=" + n);
        System.out.println("k=" + k);
    }
}
