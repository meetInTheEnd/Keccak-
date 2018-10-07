package zzzqi.keccak;

final class KeccakState200 extends KeccakShortState {


    private static final byte LANE_LENGTH = 8;

    private static final int LANE_MASK = 0xff;

    private static final byte NUMBER_OF_ROUNDS_PER_PERMUTATION = 18;

    private static final int[] ROUND_CONSTANTS_FOR_WIDTH_200;

    static {
        ROUND_CONSTANTS_FOR_WIDTH_200 = new int[]{
                1,
                130,
                138,
                0,
                139,
                1,
                129,
                9,
                138,
                136,
                9,
                10,
                139,
                139,
                137,
                3,
                2,
                128
        };
    }

    private static final byte[][] ROTATION_CONSTANTS_FOR_WIDTH_200;

    static {
        byte[][] rotOffsets = new byte[5][5];
        rotOffsets[0] = new byte[]{
                (byte) 0,
                (byte) 4,
                (byte) 3,
                (byte) 1,
                (byte) 2};
        rotOffsets[1] = new byte[]{
                (byte) 1,
                (byte) 4,
                (byte) 2,
                (byte) 5,
                (byte) 2};
        rotOffsets[2] = new byte[]{
                (byte) 6,
                (byte) 6,
                (byte) 3,
                (byte) 7,
                (byte) 5};
        rotOffsets[3] = new byte[]{
                (byte) 4,
                (byte) 7,
                (byte) 1,
                (byte) 5,
                (byte) 0};
        rotOffsets[4] = new byte[]{
                (byte) 3,
                (byte) 4,
                (byte) 7,
                (byte) 0,
                (byte) 6
        };
        ROTATION_CONSTANTS_FOR_WIDTH_200 = rotOffsets;
    }

    @Override
    final byte getLaneLengthInBits() {
        return LANE_LENGTH;
    }

    @Override
    final byte getNumberOfRoundsPerPermutation() {
        return NUMBER_OF_ROUNDS_PER_PERMUTATION;
    }

    @Override
    final int getLaneMask() {
        return LANE_MASK;
    }

    @Override
    final byte getRotationConstantForLane(int x, int y) {
        assert x >= 0 && x < 5;
        assert y >= 0 && y < 5;
        return ROTATION_CONSTANTS_FOR_WIDTH_200[x][y];
    }

    @Override
    final int getRoundConstantForRound(int roundIndex) {
        assert roundIndex >= 0 && roundIndex < NUMBER_OF_ROUNDS_PER_PERMUTATION;
        return ROUND_CONSTANTS_FOR_WIDTH_200[roundIndex];
    }

}

