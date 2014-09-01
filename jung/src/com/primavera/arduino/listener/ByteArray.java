package com.primavera.arduino.listener;

class ByteArray {

    public byte[] mByteArray = new byte[1];
    private int mUsedLength;// 현재의 길이
    public boolean mShowInAscii;

    //appending array
    void add(byte[] newArray) {
        
        while (mUsedLength + newArray.length > mByteArray.length) {
            byte[] tmpArray = new byte[mByteArray.length * 2];
            System.arraycopy(mByteArray, 0, tmpArray, 0, mUsedLength);//mByteArray 내용을 temArray로 카피
            mByteArray = tmpArray;
        }

       
        System.arraycopy(newArray, 0, mByteArray, mUsedLength, newArray.length);
        mUsedLength += newArray.length;
    }

    
    void toggleCoding() {
        mShowInAscii = !mShowInAscii;
    }

    @Override
    public String toString() {
    	// mByteArray의 내용을 StringBuilder를 이용하여 return 한다
        StringBuilder hexStr = new StringBuilder();

        if (mShowInAscii) {
        	//mByteArray의 각 원소를 검사해 숫자나 문자라면 hexStr에 스트링으로 appending 한다
        	//숫자나 문자가 아니라면 . 를 appending 한다
            for (int i = 0; i < mUsedLength; i++) {
              //  if (Character.isLetterOrDigit(mByteArray[i])) {
                    hexStr.append(new String(new byte[] {mByteArray[i]}));
             //   } else {
              //      hexStr.append('.');
               // }
            }
        } else {
            for (int i = 0; i < mUsedLength; i++) {
                hexStr.append(String.format("%1$02X", mByteArray[i]));
                hexStr.append(" ");
            }
        }

        return hexStr.toString();
    }
}