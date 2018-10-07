package zzzqi.keccak;

abstract class KeccakState {
    private static final boolean USE_BEBIGOKIMISA = true;
    abstract byte getLaneLengthInBits();
    abstract byte getNumberOfRoundsPerPermutation();
    void absorb(byte[] input,int inputLengthInBits,short bitrate) {
        assert input !=null;
        assert inputLengthInBits >= 0;
        assert bitrate > 0;
        int inputBitIndex = 0;
        do {
            int readLength = Math.min(bitrate, inputLengthInBits-inputBitIndex);
            absorbBitsIntoState(input,inputBitIndex,readLength);
            permute();
            inputBitIndex +=bitrate;
        }while(inputBitIndex < inputLengthInBits);
    }

    void absorbBitsIntoState(byte[] input, int inputStartBitIndex,
                             int readLengthInBits) {
        byte laneLength = getLaneLengthInBits();
        assert input != null;
        assert inputStartBitIndex >= 0;
        assert readLengthInBits >= 0 && readLengthInBits <= laneLength * 25;
        int inputBitIndex = inputStartBitIndex;
        int readRemaining = readLengthInBits;
        for (int y = 0; y < 5; ++y) {
            for (int x = 0; x < 5; ++x) {
                if (inputBitIndex % Byte.SIZE == 0 && readRemaining
                        >= laneLength) {
                    absorbEntireLaneIntoState(input, inputBitIndex, x, y);
                    inputBitIndex += laneLength;
                    readRemaining -= laneLength;
                } else {
                    absorbBitByBitIntoState(input, inputBitIndex, readRemaining,
                            x, y);
                    return;
                }
            }
        }
    }
    abstract void absorbEntireLaneIntoState(byte[] input, int inputBitIndex,int x, int y);

    abstract void absorbBitByBitIntoState(byte[] input, int inputStartBitIndex,int readLengthInBits, int x, int y);

    void permute() {
        if (USE_BEBIGOKIMISA) {
            applyComplementingPattern();
        }
        byte roundsPerPermutation = getNumberOfRoundsPerPermutation();
        for (int roundIndex = 0; roundIndex < roundsPerPermutation;
             ++roundIndex) {
            permutationRound(roundIndex);
        }
        if (USE_BEBIGOKIMISA) {
            applyComplementingPattern();
        }
    }

    abstract void applyComplementingPattern();

    private void permutationRound(int roundIndex) {
        assert roundIndex >= 0 && roundIndex < getNumberOfRoundsPerPermutation();
        theta();
        rhoPi();
        if (USE_BEBIGOKIMISA) {
            chiWithLaneComplementingTransform();
        } else {
            chi();
        }
        iota(roundIndex);
    }

    abstract void theta();

    abstract void rhoPi();

    abstract void chi();

    abstract void chiWithLaneComplementingTransform();

    abstract void iota(int roundIndex);

    byte[] squeeze(short bitrate, int outputLengthInBits) {
        assert bitrate > 0;
        assert outputLengthInBits > 0;
        byte[] output = createOutputArray(outputLengthInBits);
        int writeLength = Math.min(bitrate, outputLengthInBits);
        squeezeBitsFromState(output, 0, writeLength);
        for (int outputBitIndex = bitrate; outputBitIndex < outputLengthInBits;
             outputBitIndex += bitrate) {
            permute();
            writeLength = Math.min(bitrate, outputLengthInBits - outputBitIndex);
            squeezeBitsFromState(output, outputBitIndex, writeLength);
        }
        return output;
    }

    private byte[] createOutputArray(int outputLengthInBits) {
        assert outputLengthInBits > 0;
        int requiredBytes = outputLengthInBits / Byte.SIZE;
        if (outputLengthInBits % Byte.SIZE != 0) {
            ++requiredBytes;
        }
        return new byte[requiredBytes];
    }

    private void squeezeBitsFromState(byte[] output, int outputStartBitIndex,
                                      int writeLength) {
        byte laneLength = getLaneLengthInBits();
        assert output != null;
        assert outputStartBitIndex >= 0;
        assert writeLength >= 0;
        // TODO: Adapt this method for lanes of length 1, 2, and 4 bits once KATs are found.
        assert laneLength >= Byte.SIZE;
        int outputBitIndex = outputStartBitIndex;
        int outputStopIndex = outputStartBitIndex + writeLength;
        for (int y = 0; y < 5; ++y) {
            for (int x = 0; x < 5; ++x) {
                if (outputBitIndex == outputStopIndex) {
                    return;
                }
                if (outputBitIndex % Byte.SIZE == 0 && writeLength
                        - outputBitIndex
                        >= laneLength) {
                    squeezeEntireLaneIntoOutput(x, y, output, outputBitIndex);
                    outputBitIndex += laneLength;
                } else {
                    outputBitIndex = squeezeLaneBitByBitIntoOutput(output,
                            outputBitIndex, outputStopIndex, x, y);
                }
            }
        }
    }

    abstract void squeezeEntireLaneIntoOutput(int x, int y, byte[] output,
                                              int outputBitIndex);

    abstract int squeezeLaneBitByBitIntoOutput(byte[] output, int outputBitIndex,
                                               int outputStopIndex, int x, int y);

    @Override
    public final boolean equals(Object obj) {
        throw new AssertionError(
                "The equals method of KeccakState is not intended for use.");
    }

    @Override
    public final int hashCode() {
        throw new AssertionError(
                "The hashCode method of KeccakState is not intended for use.");
    }

    protected static boolean isInputBitHigh(byte[] input, int inputBitIndex) {
        assert input != null;
        assert inputBitIndex >= 0 && inputBitIndex < input.length * Byte.SIZE;
        int inputByteIndex = inputBitIndex / Byte.SIZE;
        int inputByteBitIndex = inputBitIndex % Byte.SIZE;
        return 0 != (input[inputByteIndex] & (1 << inputByteBitIndex));
    }
    protected static void setOutputBitHigh(byte[] output, int outputBitIndex) {
        assert output != null;
        assert outputBitIndex >= 0;
        int outputByteIndex = outputBitIndex / Byte.SIZE;
        byte outputByteBitIndex = (byte) (outputBitIndex % Byte.SIZE);
        byte byteBitValue = (byte) (1 << outputByteBitIndex);
        output[outputByteIndex] += byteBitValue;
    }

}
