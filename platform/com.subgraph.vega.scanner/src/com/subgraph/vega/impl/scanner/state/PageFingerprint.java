package com.subgraph.vega.impl.scanner.state;

public class PageFingerprint {
	
	public static PageFingerprint generateFromCodeAndString(int code, String body) {
		final PageFingerprint fp = new PageFingerprint();
		fp.setCode(code);
		if(body == null || body.isEmpty())
			return fp;
		boolean inSpace = false;
		int clen = 0;
		
		for(int i = 0; i < body.length(); i++) {
			char c = body.charAt(i);
			if(c <= 0x20 || c == '<' || c == '>' || c == '\'' || c == '"') {
				if(!inSpace) {
					inSpace = true;
					fp.addWordLength(clen);
					clen = 0;
				} else {
					clen++;
				}
			} else {
				if(inSpace) {
					inSpace = false;
					fp.addWordLength(clen);
					clen++;
				} else {
					clen++;
				}
			}
		}
		return fp;
	}
	
	private final static int FP_SIZE = 10;
	private final static int FP_MAX_LEN = 30;
	private final static double FP_T_REL = 5.0d;
	private final static double FP_T_ABS = 6.0d;
	private final static int FP_B_FAIL = 3;
	
	
	private final int[] fpData = new int[FP_SIZE];
	private int fpCode;
	
	public void setCode(int code) {
		this.fpCode = code;
	}
	
	public int getCode() {
		return fpCode;
	}
	
	public void addWordLength(int length) {
		if(length <= FP_MAX_LEN) {
			fpData[length % FP_SIZE]++;
		}
	}
	
	public boolean isSame(PageFingerprint other) {
		if(other == null || other.fpCode != fpCode)
			return false;
		int totalDiff = 0;
		int totalScale = 0;
		int bucketFail = 0;
		for(int i = 0; i < FP_SIZE; i++) {
			int diff = fpData[i] - other.fpData[i];
			int scale = fpData[i] + other.fpData[i];
			if( (Math.abs(diff) > (1.0d + (scale * FP_T_REL / 100.0))) || (Math.abs(diff) > FP_T_ABS)) {
				bucketFail++;
				if(bucketFail > FP_B_FAIL)
					return false;
			}
			totalDiff += diff;
			totalScale += scale;
		}
		return Math.abs(totalDiff) < (1.0d + (totalScale * FP_T_REL / 100.0));
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("FP: (code=");
		sb.append(fpCode);
		sb.append(") [");
		for(int i = 0; i < FP_SIZE; i++) {
			if(i > 0)
				sb.append(", ");
			sb.append(fpData[i]);
		}
		sb.append("]");
		return sb.toString();
	}
	
	@Override
	public boolean equals(Object other) {
		if(this == other) {
			return true;
		} else if(other instanceof PageFingerprint) {
			final PageFingerprint that = (PageFingerprint) other;
			return that.fpCode == this.fpCode && that.fpData.equals(this.fpData);
		} else {
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		return fpCode * 47 + fpData.hashCode();
	}
	
	
}
