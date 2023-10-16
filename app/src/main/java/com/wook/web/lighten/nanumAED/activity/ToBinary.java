package com.wook.web.lighten.nanumAED.activity;

public class ToBinary {

    String bin="";
    String bin2="";
    String[] hexa = { "0000", "0001", "0010", "0011", "0100",
            "0101", "0110", "0111", "1000", "1001", "1010",
            "1011", "1100", "1101", "1110", "1111" };

    public ToBinary(String bin) {
        this.bin = bin;
    }

    public String hexTobin(){

        for (int i = 0; i < 2; i++) {
            switch (bin.charAt(i)) {
                case '0':
                    bin2 += hexa[0];
                    break;
                case '1':
                    bin2 += hexa[1];
                    break;
                case '2':
                    bin2 += hexa[2];
                    break;
                case '3':
                    bin2 += hexa[3];
                    break;
                case '4':
                    bin2 += hexa[4];
                    break;
                case '5':
                    bin2 += hexa[5];
                    break;
                case '6':
                    bin2 += hexa[6];
                    break;
                case '7':
                    bin2 += hexa[7];
                    break;
                case '8':
                    bin2 += hexa[8];
                    break;
                case '9':
                    bin2 += hexa[9];
                    break;
                case 'A':
                    bin2 += hexa[10];
                    break;
                case 'B':
                    bin2 += hexa[11];
                    break;
                case 'C':
                    bin2 += hexa[12];
                    break;
                case 'D':
                    bin2 += hexa[13];
                    break;
                case 'E':
                    bin2 += hexa[14];
                    break;
                case 'F':
                    bin2 += hexa[15];
                    break;

            }
        }

        int tot = 0;
        int d = 1;

        for(int a = bin2.length() ; a > 0 ; a --){

            String str = bin2.substring(a -1, a);

            if(Integer.parseInt(str) >= 2){
                System.out.println("잘못된 입력입니다.");
                break;
            }
            tot = tot + ( d * Integer.parseInt(str));

            d = d * 2;
        }


        return String.valueOf(tot);
    }
}

