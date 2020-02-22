package com.trent.queo.lwdic.container.utils;

public class TypeSignatureConverter {

	public static  String convertTypeSignatureToClassName(String typeSignature) {
		if (typeSignature.startsWith("L")) {
			typeSignature = typeSignature.substring(1);
		}
		typeSignature = typeSignature
				.replace("/", ".")
				.replace(";", "");
		return typeSignature;
	}

}
