package com.example.bluetooththird;

import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;


import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;

class Detect {


    public static void write(String filename, double[] x) throws IOException {
        BufferedWriter outputWriter = null;
        outputWriter = new BufferedWriter(new FileWriter(filename));
        for (int i = 0; i < x.length; i++) {
            outputWriter.write(String.valueOf(x[i]));
            outputWriter.newLine();
        }
        outputWriter.flush();
        outputWriter.close();
    }

    public static double[] xcorr(double[] a, double[] b) {
        int len = a.length;
        if (b.length > a.length)
            len = b.length;

        return xcorr(a, b, len - 1);

        // // reverse b in time
        // double[] brev = new double[b.length];
        // for(int x = 0; x < b.length; x++)
        //     brev[x] = b[b.length-x-1];
        //
        // return conv(a, brev);
    }

    /**
     * Computes the cross correlation between sequences a and b.
     * maxlag is the maximum lag to
     */
    public static double[] xcorr(double[] a, double[] b, int maxlag) {
        double[] y = new double[2 * maxlag + 1];
        Arrays.fill(y, 0);

        for (int lag = b.length - 1, idx = maxlag - b.length + 1; lag > -a.length; lag--, idx++) {
            if (idx < 0)
                continue;

            if (idx >= y.length)
                break;

            // where do the two signals overlap?
            int start = 0;
            // we can't start past the left end of b
            if (lag < 0) {
                //System.out.println("b");
                start = -lag;
            }

            int end = a.length - 1;
            // we can't go past the right end of b
            if (end > b.length - lag - 1) {
                end = b.length - lag - 1;
                //System.out.println("a "+end);
            }

            //System.out.println("lag = " + lag +": "+ start+" to " + end+"   idx = "+idx);
            for (int n = start; n <= end; n++) {
                //System.out.println("  bi = " + (lag+n) + ", ai = " + n);
                y[idx] += a[n] * b[lag + n];
            }
            //System.out.println(y[idx]);
        }

        return (y);
    }

    public static double isit(double[] recorded, double[] snipped) {


        double[] ne = xcorr(snipped, recorded);

        try {
            write("/storage/emulated/0/dude.txt", ne);

        } catch (Exception e) {

        }


        double largestCorr = 0;
        double largest_counter = 0;
        double secondlargest_counter = 0;
        double secondlargestCorr = 0;


        for (int s = 0; s <= ne.length - 1; s++) {

            if (ne[s] > largestCorr) {

                if (s >= (largest_counter + 3000)) { // absolut
                    secondlargestCorr = largestCorr;
                    secondlargest_counter = largest_counter;
                }
                largestCorr = ne[s];
                largest_counter = s;
            } else if (ne[s] > secondlargestCorr && s >= Math.abs(largest_counter + 3000)) {
                secondlargestCorr = ne[s];
                secondlargest_counter = s;
            }
        }




/*

        String strFilePath = "/storage/emulated/0/r.txt";
        try {
            FileOutputStream fos = new FileOutputStream(strFilePath);
            DataOutputStream dos = new DataOutputStream(fos);
            for(int i = 0; i <= ne.length-1 ; i++){
                dos.writeDouble(ne[i]);
            }
            dos.close();
        }catch (Exception e){

        }
*/


        double difference = Math.abs(largest_counter - secondlargest_counter);
        return difference;


/*
        //maxima finden in normaler aufnahme


        double largestCorr = 0;
        double largest_counter = 0;
        double secondlargest_counter = 0;
        double secondlargestCorr = 0;


        for (int s = 0; s <= recorded.length - 1; s++) {

            if (recorded[s] > largestCorr) {

                if (s >= (largest_counter + 3000) && s <= (largest_counter + 10000)) { // absolut
                    secondlargestCorr = largestCorr;
                    secondlargest_counter = largest_counter;
                }


                largestCorr = recorded[s];
                largest_counter = s;
            } else if (recorded[s] > secondlargestCorr && s >= Math.abs(largest_counter + 3000) && s <= Math.abs(largest_counter + 10000)) {
                secondlargestCorr = recorded[s];
                secondlargest_counter = s;
            }
        }

        return Math.abs(largest_counter - secondlargest_counter);

        */
/*

        //Pearsonmethode neu
        PearsonsCorrelation correlation = new PearsonsCorrelation();
        double correlation_calculated = 0;
        int VAR = 2205;
        double largest_counter = 0;
        double secondlargest_counter = 0;
        double largestCorr = 0;
        double secondlargestCorr = 0;
        double[] recorded_sliced;
        double lengthOfSignal = recorded.length;
        snipped = Arrays.copyOfRange(snipped, 0, VAR);

        for (int s = 0; s <= lengthOfSignal - VAR; s++) {
            recorded_sliced = Arrays.copyOfRange(recorded, s, VAR + s);
            correlation_calculated = correlation.correlation(snipped, recorded_sliced);
            if (correlation_calculated > largestCorr) {
                if (s >= (largest_counter + 3000) && s <= (largest_counter + 10000)) { // absolut
                    secondlargestCorr = largestCorr;
                    secondlargest_counter = largest_counter;
                }
                largestCorr = correlation_calculated;
                largest_counter = s;
            } else if (correlation_calculated > secondlargestCorr && s >= Math.abs(largest_counter + 3000) && s <= Math.abs(largest_counter + 10000)) {
                secondlargestCorr = correlation_calculated;
                secondlargest_counter = s;
            }
        }

        double difference = largest_counter - secondlargest_counter;
        return difference;
*/
    }
}