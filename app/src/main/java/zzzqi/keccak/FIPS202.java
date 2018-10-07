package zzzqi.keccak;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.function.UnaryOperator;

public class FIPS202 {
    private FIPS202() {

    }

    public enum HashFunction implements UnaryOperator<byte[]>{
        SHA3_224((short)1152,(short)448,"01",224),
        //short类型是16位，有符号的二进制补码表示的整数，可以像byte那样节省空间
        SHA3_256((short)1088,(short)512,"01",256),
        SHA3_384((short)832,(short)768,"01",384),
        SHA3_512((short)576,(short)1024,"01",512);

        private final short bitrate;
        private final short capacity;
        private final String suffixBits;
        private final int outputLengthInBits;

        private KeccakSponge spongeFunction;//返回一个keccakSponge类

        private HashFunction(short r,short c,String d,int l) {
            bitrate =r;
            capacity = c;
            suffixBits = d;
            outputLengthInBits = l;
        }
        private KeccakSponge getSpongeFunction() {
            if (spongeFunction ==null) {
                initialiseSpongeFunction();
            }
            return spongeFunction;
        }

        private synchronized void initialiseSpongeFunction() {
            //synshronized是一种同步锁，被修饰的代码块被称为同步语句块，
            if(spongeFunction !=null) {
                return ;
            }
            spongeFunction = new KeccakSponge(bitrate,capacity,
                    suffixBits,outputLengthInBits);
        }

        /**此方法不会修改给定的消息字节数组。但是，为了避免出现问题，在使用消息字节数组计算散列时，
         * 任何其他线程都不应该访问它。如果消息来自共享资源，则获取副本并将副本传递给此方法。
         */
        public byte[] apply(byte[] message) {
            Objects.requireNonNull(message);
            //当传入的参数不为null时，返回参数本身，反之，抛出一个NullPointerException异常
            return getSpongeFunction().apply(message);
        }
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("SHA3-");
            sb.append(outputLengthInBits);
            return sb.toString();
        }
    }

    public enum ExtendableOutputFunction{
        SHAKE128((short)1344,(short)256,"1111"),
        SHAKE256((short)1088,(short)512,"1111"),
        RawSHAKE128((short)1344,(short)256,"11"),
        RawHAKE256((short)1088,(short)512,"11");

        private final short bitrate;
        private final short capacity;
        private final String suffixBits;

        private ExtendableOutputFunction(short r,short c,String suffixBits) {
            bitrate = r;
            capacity = c;
            this.suffixBits = suffixBits;
        }

        public KeccakSponge withOutputLength(int outputLengthInBits) {
            if(outputLengthInBits <1) {
                throw new IllegalArgumentException(
                        "outputLengthInBits must be greate than zero");
            }
            return new KeccakSponge(bitrate,capacity,suffixBits,outputLengthInBits);
        }
    }

    @Override
    public String toString() {
        return this.name();
    }

    private String name() {
        // TODO Auto-generated method stub
        return null;
    }
    //把字节数组转换成String
    public static String hexFromBytes(byte[] bytes) {
        Objects.requireNonNull(bytes,"Parameter 'bytes' cannot be null.");
        StringBuilder hexString = new StringBuilder(bytes.length*2);
        for(byte b:bytes) {
            appendByteAsHexPair(b,hexString);
        }
        return hexString.toString();
    }
    //单个字节转换成十六进制字符
    private static void appendByteAsHexPair(byte b,StringBuilder sb) {
        assert sb !=null;
        byte leastSignificantHalf = (byte)(b&0x0f);
        byte mostSignificanHalf =(byte)(b>>4&0x0f);
        sb.append(getHexDigiWithValue(mostSignificanHalf));
        sb.append(getHexDigiWithValue(leastSignificantHalf));

    }
    //单个字节转换成16进制字符
    private static char getHexDigiWithValue(byte value) {
        assert value >=0&&value <=16;
        if(value <10) {
            return (char)('0'+value);
        }
        return (char)('A'+value-10);
    }
    //把二进制字符串转变为十六进制字符串。
    public static String hexFromBinary(String bitString) {
        Objects.requireNonNull(bitString,
                "Parameter `bitString` must not be null.");
        StringBuilder hexString = new StringBuilder(
                (bitString.length() + 8 - 1) / 8);
        for (int bitIndex = 0; bitIndex < bitString.length(); bitIndex += 8) {
            byte byteValue = byteValueOfBinaryAtIndex(bitIndex, bitString);
            appendByteAsHexPair(byteValue, hexString);
        }
        return hexString.toString();
    }
    //输入二进制字符串和索引位置，输出字节
    private static byte byteValueOfBinaryAtIndex(int bitIndex, String bitString) {
        int bitsRemaining = bitString.length() - bitIndex;
        int byteBitStopIndex = Math.min(8, bitsRemaining);//
        byte byteValue = (byte) 0;
        for (int byteBitIndex = 0; byteBitIndex < byteBitStopIndex;
             ++byteBitIndex) {
            if (bitString.charAt(bitIndex + byteBitIndex) == '0') {
                continue;
            }
            byteValue += (byte) (1 << byteBitIndex);
        }
        return byteValue;
    }

    //16进制字符串，输出字节数组
    public static byte[] bytesFromHex(String hex) {
        Objects.requireNonNull(hex, "Parameter `hex` cannot be null.");
        int hexLength = hex.length();
        if (hexLength % 2 != 0) {
            throw new IllegalArgumentException(
                    "Hexadecimal string must be composed of hexadecimal pairs.");
        }
        byte[] bytes = new byte[hexLength / 2];
        for (int charIndex = 0; charIndex < hexLength; charIndex += 2) {
            bytes[charIndex / 2] = byteValueOfHexPairAtIndex(hex, charIndex);
        }
        return bytes;
    }
    //
    private static byte byteValueOfHexPairAtIndex(String hex, int charIndex) {
        assert hex != null;
        assert charIndex >= 0;
        char mostSignificantHexDigit = hex.charAt(charIndex);
        byte hexPairValue = (byte) (16
                * valueOfHexDigit(mostSignificantHexDigit));
        char leastSignificantHexDigit = hex.charAt(charIndex + 1);
        hexPairValue += valueOfHexDigit(leastSignificantHexDigit);
        return hexPairValue;
    }

    private static byte valueOfHexDigit(char hexDigit) {
        if ('0' <= hexDigit && hexDigit <= '9') {
            return (byte) (hexDigit - '0');
        }
        if ('A' <= hexDigit && hexDigit <= 'F') {
            return (byte) (10 + hexDigit - 'A');
        }
        if ('a' <= hexDigit && hexDigit <= 'f') {
            return (byte) (10 + hexDigit - 'a');
        }
        throw new IllegalArgumentException(
                "hexDigit must be from character set [0-9A-F] (case insensitive).");
    }


    public static String binaryFromHex(String hex, int bitLimit) {
        validateHexString(hex);
        validateBitLimit(hex, bitLimit);
        int byteLimit = (bitLimit + 8 - 1) / 8;
        int hexDigitLimit = byteLimit * 2;
        StringBuilder bitString = new StringBuilder(bitLimit);
        for (int hexCharIndex = 0, bitsSoFar = 0;
             hexCharIndex < hexDigitLimit && bitsSoFar < bitLimit;
             hexCharIndex += 2, bitsSoFar += 8) {
            byte hexPairValue = byteValueOfHexPairAtIndex(hex, hexCharIndex);
            int bitsRequiredFromHexPair = Math.min(8, bitLimit - bitsSoFar);
            appendBitsFromByte(hexPairValue, bitsRequiredFromHexPair, bitString);
        }
        return bitString.toString();
    }

    private static void validateHexString(String hex) {
        Objects.requireNonNull(hex, "Parameter `hex` cannot be null.");
        int hexLength = hex.length();
        if (hexLength % 2 != 0) {
            throw new IllegalArgumentException(
                    "String `hex` must contain an even number of hex digits.");
        }
        for (int charIndex = 0; charIndex < hexLength; ++charIndex) {
            char hexDigit = hex.charAt(charIndex);
            if (!isValidHexDigit(hexDigit)) {
                throw new IllegalArgumentException(
                        "String `hex` can only contain hex digits [0-9A-F] (case insensitive).");
            }
        }
    }

    private static void validateBitLimit(String hex, int bitLimit) {
        if (bitLimit < 0) {
            throw new IllegalArgumentException("bitLimit cannot be negative.");
        }
        if (bitLimit > hex.length() * Byte.SIZE / 2) {
            throw new IllegalArgumentException(
                    "bitLimit cannot exceed the number of bits represented by the hex string.");
        }
    }

    private static boolean isValidHexDigit(char hexDigit) {
        if ('0' <= hexDigit && hexDigit <= '9') {
            return true;
        }
        if ('A' <= hexDigit && hexDigit <= 'F') {
            return true;
        }
        if ('a' <= hexDigit && hexDigit <= 'f') {
            return true;
        }
        return false;
    }

    private static void appendBitsFromByte(byte value, int bitsRequired,
                                           StringBuilder bitString) {
        for (byte bitIndex = 0; bitIndex < bitsRequired; ++bitIndex) {
            boolean bitHigh = (value & (byte) (1 << bitIndex)) != 0;
            bitString.append(bitHigh ? '1' : '0');
        }
    }
}
