package zzzqi.keccak;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public final class KeccakSponge  {
    private static final Set<Short> VALID_WIDTHS;
    static {
        Set<Short> widths = new HashSet<>(16);
        widths.addAll(Arrays.asList(new Short[]{
                25,
                50,
                100,
                200,
                400,
                800,
                1600
        }));
        VALID_WIDTHS = Collections.unmodifiableSet(widths);
        //返回set的不可修改视图
    }

    private final short bitrate;
    private final short capacity;
    private final byte laneLength;
    private final int outputLengthInBits;
    private final String suffixBits;

    public int getBitrate() {
        return bitrate;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getPermutationWidth() {
        return bitrate+capacity;
    }

    public int getLaneLength() {
        return laneLength;
    }

    public int getNumberOfRoundsPermutation() {
        return KeccakUtilities.getNumberOfRoundsPerPermutationWithLaneLength(laneLength);
    }

    public int getOutputLengthInBits() {
        return outputLengthInBits;
    }
    public Optional<String> getSuffixBits() {
        if (suffixBits.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(suffixBits);
        }
    }
    public KeccakSponge(int bitrate,int capacity,String suffixBits,int outputLength) {
        validateBitrate(bitrate);
        validateCapacity(capacity);
        validateSuffixBits(suffixBits);
        validateOutputLength(outputLength);
        short width = (short) (bitrate+capacity);
        validatePermutationWidth(width);
        this.bitrate = (short)bitrate;
        this.capacity = (short)capacity;
        this.suffixBits = suffixBits;
        this.laneLength = (byte) (width/25);
        this.outputLengthInBits = outputLength;
    }

    public byte[] apply(byte[] message) {
        return apply(message.length*Byte.SIZE,message);
    }

    public byte[] apply(int messageLengthInBits,byte[] message) {
        validateMessageLength(message,messageLengthInBits);
        int inputLengthInBits = calculateTotalInputLength(messageLengthInBits);//计算填充后的明文长度
        byte[] input = createSufficientlyLargeByteArray(inputLengthInBits);     //计算字节数组长度，返回一个具有合适长度的字节数组
        moveMessageBitsIntoInput(message, messageLengthInBits, input);          //message byte数组复制到input数组
        appendDomainSuffixToInput(input, messageLengthInBits);              //
        padInput(input, messageLengthInBits);
        KeccakState state = createKeccakStateForLength(laneLength);
        state.absorb(input, inputLengthInBits, bitrate);
        byte[] hash = state.squeeze(bitrate, outputLengthInBits);
        return hash;
    }

    private KeccakState createKeccakStateForLength(int laneLength) {
        switch (laneLength) {
            case 64:
                return new KeccakState1600();
            case 32:
                return new KeccakState800();
            case 16:
                return new KeccakState400();
            case 8:
                return new KeccakState200();
            default:
                throw new UnsupportedOperationException(
                        "Permutation width currently not supported.");
        }
    }

    public byte[] apply(InputStream stream) throws IOException {
        // TODO: Add support for cases where bitrate is not divisible by 8.
        requireWholeByteBitrate(bitrate);
        Objects.requireNonNull(stream);
        KeccakState state = createKeccakStateForLength(laneLength);
        byte[] block = createSufficientlyLargeByteArray(bitrate);
        int finalBlockMessageBits = absorbInitialStreamBlocksIntoState(stream,
                block, state);
        byte[] finalBlock = prepareFinalBlockArray(finalBlockMessageBits, block);
        appendDomainSuffixToInput(finalBlock, finalBlockMessageBits);
        padInput(finalBlock, finalBlockMessageBits);
        state.absorb(finalBlock, finalBlock.length * Byte.SIZE, bitrate);
        byte[] hash = state.squeeze(bitrate, outputLengthInBits);
        return hash;
    }
    //计算填充后的明文长度
    private int calculateTotalInputLength(int messageLengthInBits) {
        assert messageLengthInBits >= 0;
        int minimumPaddedLength = calculateMinimumLengthAfterPadding(
                messageLengthInBits);
        if (minimumPaddedLength % bitrate == 0) {
            return minimumPaddedLength;
        } else {
            return minimumPaddedLength + bitrate - minimumPaddedLength % bitrate;
        }
    }

    private int calculateMinimumLengthAfterPadding(int messageLengthInBits) {
        // The padding always starts and ends with a high '1' bit, so the
        // padding length will always be at least two bits.
        return messageLengthInBits + suffixBits.length() + 2;
    }
    //
    private void appendDomainSuffixToInput(byte[] input, int suffixStartBitIndex) {
        assert input != null;
        assert suffixStartBitIndex >= 0;
        assert suffixBits != null;
        for (int suffixBitIndex = 0; suffixBitIndex < suffixBits.length();
             ++suffixBitIndex) {
            boolean suffixBitHigh = suffixBits.charAt(suffixBitIndex) == '1';
            if (suffixBitHigh) {
                int targetInputBit = suffixStartBitIndex + suffixBitIndex;
                int targetInputByte = targetInputBit / Byte.SIZE;
                int targetInputByteBitIndex = targetInputBit % Byte.SIZE;
                input[targetInputByte] += 1 << targetInputByteBitIndex;
            }
        }
    }

    private void padInput(byte[] input, int messageLengthInBits) {
        assert input != null;
        assert messageLengthInBits >= 0;
        int lengthOfMessageWithSuffix = messageLengthInBits + suffixBits.
                length();
        int zeroPaddingBitsRequired = calculateZeroPaddingBitsRequired(
                messageLengthInBits);
        int padStartIndex = lengthOfMessageWithSuffix;
        int padEndIndex = lengthOfMessageWithSuffix + 1
                + zeroPaddingBitsRequired;
        setInputBitHigh(input, padStartIndex);
        setInputBitHigh(input, padEndIndex);
    }

    private int calculateZeroPaddingBitsRequired(int messageLengthInBits) {
        int bitsIncludingPadEnds = calculateMinimumLengthAfterPadding(
                messageLengthInBits);
        int zeroPaddingBitsRequired;
        if (bitsIncludingPadEnds % bitrate == 0) {
            zeroPaddingBitsRequired = 0;
        } else {
            zeroPaddingBitsRequired = bitrate - bitsIncludingPadEnds % bitrate;
        }
        return zeroPaddingBitsRequired;
    }

    private void setInputBitHigh(byte[] input, int inputBitIndex) {
        assert input != null;
        assert inputBitIndex >= 0;
        int inputByteIndex = inputBitIndex / Byte.SIZE;
        byte outputByteBitIndex = (byte) (inputBitIndex % Byte.SIZE);
        byte byteBitValue = (byte) (1 << outputByteBitIndex);
        input[inputByteIndex] += byteBitValue;
    }

    private int absorbInitialStreamBlocksIntoState(InputStream stream,
                                                   byte[] block, KeccakState state) throws IOException {
        assert stream != null;
        assert block != null;
        assert state != null;
        int bitsInCurrentBlock = readBlockFromStream(stream, block);
        while (bitsInCurrentBlock == bitrate) {
            state.absorbBitsIntoState(block, 0, bitsInCurrentBlock);
            state.permute();
            bitsInCurrentBlock = readBlockFromStream(stream, block);
        }
        return bitsInCurrentBlock;
    }


    private int readBlockFromStream(InputStream stream, byte[] block) throws
            IOException {
        assert block != null;
        assert block.length * Byte.SIZE == bitrate;
        assert stream != null;
        int filledBytes = 0;
        int readBytes = stream.read(block);
        while (readBytes > 0) {
            filledBytes += readBytes;
            readBytes = stream.read(block, filledBytes, block.length
                    - filledBytes);
        }
        if (filledBytes < block.length) {
            Arrays.fill(block, filledBytes, block.length, (byte) 0);
        }
        return filledBytes * Byte.SIZE;
    }


    private byte[] prepareFinalBlockArray(int finalBlockMessageLengthInBits,
                                          byte[] finalBlock) {
        assert finalBlockMessageLengthInBits >= 0;
        assert finalBlock != null;
        int minimumLengthAfterPadding = calculateMinimumLengthAfterPadding(
                finalBlockMessageLengthInBits);
        if (minimumLengthAfterPadding <= bitrate) {
            // The existing byte array is large enough so simply return it.
            return finalBlock;
        } else {
            return resizedFinalBlockArray(finalBlockMessageLengthInBits,
                    finalBlock, minimumLengthAfterPadding);
        }
    }

    private byte[] resizedFinalBlockArray(int finalBlockMessageLengthInBits,
                                          byte[] finalBlock, int minimumLengthAfterPadding) {
        int blocksRequired
                = divideThenRoundUp(minimumLengthAfterPadding, bitrate);
        byte[] finalBlocks = new byte[blocksRequired * bitrate / Byte.SIZE];
        int bytesToCopy = divideThenRoundUp(finalBlockMessageLengthInBits,
                Byte.SIZE);
        System.arraycopy(finalBlock, 0, finalBlocks, 0, bytesToCopy);
        return finalBlocks;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(64);
        sb.append("Keccak[");
        sb.append(getBitrate());
        sb.append(", ");
        sb.append(getCapacity());
        sb.append("](M");
        if (getSuffixBits().isPresent()) {
            sb.append(" || ");
            sb.append(getSuffixBits().get());
            sb.append(',');
        } else {
            sb.append(',');
        }
        sb.append(' ');
        sb.append(getOutputLengthInBits());
        sb.append(')');
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof KeccakSponge)) {
            return false;
        }
        KeccakSponge that = (KeccakSponge) obj;
        return this.bitrate == that.bitrate
                && this.capacity == that.capacity
                && this.outputLengthInBits == that.outputLengthInBits
                && this.suffixBits.equals(that.suffixBits);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + this.bitrate;
        hash = 41 * hash + this.capacity;
        hash = 41 * hash + Objects.hashCode(this.suffixBits);
        hash = 41 * hash + this.outputLengthInBits;
        return hash;
    }

    private static void validateBitrate(int bitrate) {
        if (bitrate < 1) {
            throw new IllegalArgumentException(
                    "bitrate must be greater than zero.");
        }
        if (bitrate % Byte.SIZE != 0) {
            // TODO: Find KATs for Keccak with non-whole-byte bitrates, and add support to this library.
            throw new UnsupportedOperationException(
                    "Currently only bitrates exactly divisible by 8 are supported.");
        }
        if (bitrate >= 1600) {
            throw new IllegalArgumentException(
                    "bitrate must be less than 1600 bits.");
        }
    }

    private static void validateSuffixBits(String suffixBits) {
        Objects.requireNonNull(suffixBits);
        int length = suffixBits.length();
        for (int index = 0; index < length; ++index) {
            char c = suffixBits.charAt(index);
            if (c != '1' && c != '0') {
                throw new IllegalArgumentException(
                        "If suffixBits is provided then it must be a bitstring. "
                                + "It can contain only digits 0 and 1 and nothing else.");
            }
        }
    }

    private static void validateCapacity(int capacity) {
        if (capacity < 1) {
            throw new IllegalArgumentException(
                    "capacity must be greater than zero.");
        }
        if (capacity >= 1600) {
            throw new IllegalArgumentException(
                    "capacity must be less than 1600 bits.");
        }
    }

    private static void validateOutputLength(int outputLength) {
        if (outputLength < 1) {
            throw new IllegalArgumentException(
                    "outputLength must be greater than zero.");
        }
    }

    private static void validatePermutationWidth(short width) {
        // TODO: Add support for smaller widths (with lanes of less than one byte).
        // (Have not been able to find KATs for widths below 200 bits.)
        if (width < 200) {
            throw new UnsupportedOperationException(
                    "Support is not yet in place for permutations widths smaller than 200 bits.");
        }
        if (!VALID_WIDTHS.contains(width)) {
            List<Short> validWidthList = new ArrayList<>(VALID_WIDTHS);
            //validWidthList.sort((a, b) -> a - b);

            throw new IllegalArgumentException(
                    "Sum of bitrate and capacity must equal a valid width: "
                            + validWidthList + ".");
        }
    }

    private static void validateMessageLength(byte[] message,
                                              int messageLengthInBits) {
        if (messageLengthInBits < 0) {
            throw new IllegalArgumentException(
                    "messageLengthInBits cannot be negative.");
        }
        if (messageLengthInBits > message.length * Byte.SIZE) {
            throw new IllegalArgumentException(
                    "messageLengthInBits cannot be greater than the bit length of the message byte array.");
        }
    }

    //
    private static void moveMessageBitsIntoInput(byte[] message,
                                                 int messageLengthInBits, byte[] input) {
        assert message != null;
        assert messageLengthInBits >= 0;
        assert input != null;
        if (messageLengthInBits % Byte.SIZE == 0) {
            System.arraycopy(message, 0, input, 0, messageLengthInBits
                    / Byte.SIZE);
        } else {
            partialByteCopy(message, input, messageLengthInBits);
        }
    }


    private static void partialByteCopy(byte[] source, byte[] destination,
                                        int bitLimit) {
        assert source != null;
        assert destination != null;
        assert bitLimit >= 0;
        int wholeByteCount = bitLimit / Byte.SIZE;
        System.arraycopy(source, 0, destination, 0, wholeByteCount);
        int remainingBits = bitLimit % Byte.SIZE;
        for (int bitIndex = 0; bitIndex < remainingBits; ++bitIndex) {
            int bitValue = (1 << bitIndex);
            boolean sourceBitHigh = (source[wholeByteCount] & bitValue) != 0;
            if (sourceBitHigh) {
                destination[wholeByteCount] += bitValue;
            }
        }
    }

    private static void requireWholeByteBitrate(int bitrate) {
        assert bitrate > 0;
        if (bitrate % Byte.SIZE != 0) {
            throw new UnsupportedOperationException(
                    "bitrate must be divisible by eight in order to process byte stream.");
        }
    }


    private static byte[] createSufficientlyLargeByteArray(int bitCount) {
        assert bitCount > 0;
        int bytesRequired = divideThenRoundUp(bitCount, Byte.SIZE);
        return new byte[bytesRequired];
    }


    private static int divideThenRoundUp(int dividend, int divisor) {
        assert dividend >= 0;
        assert divisor > 0;
        if (dividend == 0) {
            return 0;
        }
        if (dividend % divisor == 0) {
            return dividend / divisor;
        } else {
            return 1 + dividend / divisor;
        }
    }


    public static void main(String[] args) {
        KeccakSponge spongeFunction = new KeccakSponge(576, 1024, "", 512);
        byte[] message = new byte[]{(byte) 19};
        byte[] hash = spongeFunction.apply(5, message);
        for (int i = 0; i < 900000; ++i) {
            hash = spongeFunction.apply(hash);
        }
    }

}

