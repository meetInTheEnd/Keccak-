package zzzqi.keccak;
import java.util.BitSet;

class KeccakUtilities {
    private static byte[][] RAW_ROTATION_CONSTANTS;

    static {
        //循环移位偏移表
        byte[][] rotOffsets = new byte[5][5];
        rotOffsets[0] = new byte[] {(byte)0,
                (byte) 36,
                (byte) 3,
                (byte) 41,
                (byte) 18};
        rotOffsets[1] = new byte[]{(byte) 1,
                (byte) 44,
                (byte) 10,
                (byte) 45,
                (byte) 2};
        rotOffsets[2] = new byte[]{(byte) 62,
                (byte) 6,
                (byte) 43,
                (byte) 15,
                (byte) 61};
        rotOffsets[3] = new byte[]{(byte) 28,
                (byte) 55,
                (byte) 25,
                (byte) 21,
                (byte) 56};
        rotOffsets[4] = new byte[]{(byte) 27,
                (byte) 20,
                (byte) 39,
                (byte) 8,
                (byte) 14};
        RAW_ROTATION_CONSTANTS = rotOffsets;
    }
    //为不同的laneLength得到不同的循环移位偏移表
    static byte[][] getRotationConstantsForLaneLength(int laneLength) {
        if(laneLength == 64) {
            return RAW_ROTATION_CONSTANTS;
        }
        byte[][] moduloRatations = new byte[5][5];

        for(int x =0;x<5;x++) {
            for(int y = 0;y < 5;y++) {
                moduloRatations[x][y]=(byte)(RAW_ROTATION_CONSTANTS [x][y]%laneLength);

            }
        }
        return moduloRatations;
    }
    //

    long[] buildRoundConstants(int laneLength) {
        assert laneLength > 0 && laneLength <=64;
        byte numberOfRoundsPerPermutation =
                getNumberOfRoundsPerPermutationWithLaneLength(laneLength);
        long[] array = new long[numberOfRoundsPerPermutation];
        int l = getBinaryExponent(laneLength);
        for(int roundIndex =0;roundIndex <numberOfRoundsPerPermutation;
            ++roundIndex) {
            long roundConstant = 0L;
            for(int j = 0; j <= l;++j) {
                int index = (int)Math.pow(2.0, j)-1;
                boolean isHigh=rc(j+7*roundIndex);
                if(isHigh) {
                    roundConstant +=1L<<index;
                }
            }
            array[roundIndex] = roundConstant;
        }
        return array;
    }

    static byte  getNumberOfRoundsPerPermutationWithLaneLength(int laneLength){

        switch(laneLength) {
            case 1:
                return 12;
            case 2:
                return 14;
            case 4:
                return 16;
            case 8:
                return 18;
            case 16:
                return 20;
            case 32:
                return 22;
            case 64:
                return 24;
            default:
                throw new IllegalArgumentException("Illegal lane size: "
                        + laneLength);
        }
    }
    //得到laneLength的二进制指数
    static byte getBinaryExponent(int laneLength) {
        switch(laneLength) {
            case 1:
                return 0;
            case 2:
                return 1;
            case 4:
                return 2;
            case 8:
                return 3;
            case 16:
                return 4;
            case 32:
                return 5;
            case 64:
                return 6;
            default:
                throw new IllegalArgumentException("Illegal lane size: "
                        + laneLength);
        }
    }
    //生成rctable
    static boolean rc(int t) {
        assert t >= 0 && t <=167;
        t = t % 255;
        if(t == 0) {
            return true;
        }
        BitSet r = new BitSet( 8+t);
        int zeroIndex = t;
        r.set(zeroIndex,true);
        for(int i = 1;i <= t; ++i) {
            --zeroIndex;
            r.set(zeroIndex, r.get(zeroIndex) ^ r.get(zeroIndex + 8));
            r.set(zeroIndex + 4, r.get(zeroIndex + 4) ^ r.get(zeroIndex + 8));
            r.set(zeroIndex + 5, r.get(zeroIndex + 5) ^ r.get(zeroIndex + 8));
            r.set(zeroIndex + 6, r.get(zeroIndex + 6) ^ r.get(zeroIndex + 8));
        }
        return r.get(zeroIndex);
    }


}

