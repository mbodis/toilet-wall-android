package com.svb.toiletwall.utils;

import java.util.Arrays;

public final class SamplingUtils {
    public static short[][] getExtremes(short[] data, int sampleSize) {
        short[][] newData = new short[sampleSize][];
        int groupSize = data.length / sampleSize;

        for (int i = 0; i < sampleSize; i++) {
            short[] group = Arrays.copyOfRange(data, i * groupSize,
                    Math.min((i + 1) * groupSize, data.length));

            // Fin min & max values
            short min = Short.MAX_VALUE, max = Short.MIN_VALUE;
            for (short a : group) {
                min = (short) Math.min(min, a);
                max = (short) Math.max(max, a);
            }
            newData[i] = new short[] { max, min };
        }

        return newData;
    }

    public static double rootMeanSquared(short[] nums)
    {
        double ms = 0;
        for (int i = 0; i < nums.length; i++)
        {
            ms += nums[i] * nums[i];
        }
        ms /= nums.length;
        return Math.sqrt(ms);
    }
}
