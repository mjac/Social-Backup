package com.mjac.socialbackup.test;

import java.security.Provider;
import java.security.Security;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

/*
 * Ciphers:
            PBEWITHSHAAND192BITAES-CBC-BC
            Rijndael
            NOEKEON
            RSA//NOPADDING
            PBEWITHSHA1AND2-KEYTRIPLEDES-CBC
            PBEWITHSHA1AND40BITRC2-CBC
            AESWrap SupportedKeyFormats
            PBEWITHMD5AND256BITAES-CBC-OPENSSL
            GOST
            DESedeWrap
            GOST28147
            SEEDWRAP
            2.16.840.1.101.3.4.1.3
            2.16.840.1.101.3.4.1.2
            2.16.840.1.101.3.4.1.5
            2.16.840.1.101.3.4.1.4
            2.16.840.1.101.3.4.22
            1.3.6.1.4.1.22554.1.2.1.2.1.42
            2.16.840.1.101.3.4.1.1
            Blowfish SupportedModes
            RC5-32
            1.3.6.1.4.1.22554.1.1.2.1.22
            PBEWITHSHAAND128BITAES-CBC-BC
            BROKENPBEWITHSHAAND2-KEYTRIPLEDES-CBC
            PBEWITHSHA1AND128BITRC2-CBC
            RSA//RAW
            HC256
            Serpent
            OLDPBEWITHSHAANDTWOFISH-CBC
            PBEWITHSHA-1AND256BITAES-CBC-BC
            DESede SupportedPaddings
            RC2 SupportedModes
            DESedeWrap SupportedKeyFormats
            OID.1.2.840.113549.1.12.1.6
            1.2.392.200011.61.1.1.1.2
            1.2.392.200011.61.1.1.1.4
            1.2.392.200011.61.1.1.1.3
            PBEWITHSHAAND40BITRC2-CBC
            OID.1.2.840.113549.1.12.1.3
            2.16.840.1.101.3.4.2
            AES
            DESEDEWRAP
            PBEWITHSHAAND256BITAES-CBC-BC
            DESede SupportedModes
            1.3.6.1.4.1.22554.1.1.2.1.42
            PBEWITHSHA1AND128BITAES-CBC-BC
            RSA//ISO9796-1PADDING
            CAMELLIA
            PBEWITHSHA1ANDRC2
            ARC4
            PBEWithSHA1AndDESede
            RC2 SupportedPaddings
            DESEDE
            PBEWITHSHA1AND128BITRC4
            RC2WRAP
            Grain128
            AESWrap SupportedModes
            ARCFOUR SupportedPaddings
            BROKENPBEWITHSHAAND3-KEYTRIPLEDES-CBC
            CAMELLIARFC3211WRAP
            PBEWITHSHA1AND192BITAES-CBC-BC
            PBEWITHSHA1AND3-KEYTRIPLEDES-CBC
            BrokenECIES
            DES SupportedPaddings
            PBEWITHSHA1ANDDESEDE
            AESWRAP
            SKIPJACK
            RSA SupportedPaddings
            DES SupportedKeyFormats
            Blowfish SupportedKeyFormats
            1.2.840.113549.1.5.11
            1.2.840.113549.1.5.10
            XTEA
            PBEWITHSHA-256AND128BITAES-CBC-BC
            PBEWITHMD5AND192BITAES-CBC-OPENSSL
            PBEWITHMD5ANDDES
            VMPC
            AESWrap SupportedPaddings
            CAMELLIAWRAP
            2.16.840.1.101.3.4.42
            1.2.840.113549.1.12.1.5
            ARCFOUR SupportedKeyFormats
            1.2.840.113549.1.12.1.6
            1.2.840.113549.1.12.1.3
            RSA SupportedModes
            1.2.840.113549.1.12.1.4
            1.2.840.113549.3.7
            1.2.840.113549.1.12.1.1
            1.2.840.113549.1.12.1.2
            1.2.840.113549.3.4
            PBEWithMD5AndDES
            AES SupportedPaddings
            DESedeWrap SupportedPaddings
            2.16.840.1.101.3.4.1.25
            RSA/ISO9796-1
            2.16.840.1.101.3.4.1.24
            2.16.840.1.101.3.4.1.23
            2.16.840.1.101.3.4.1.22
            2.16.840.1.101.3.4.1.21
            1.2.840.113549.3.2
            PBEWITHSHAAND40BITRC4
            AES SupportedModes
            2.16.840.1.101.3.4.1.42
            2.16.840.1.101.3.4.1.43
            2.16.840.1.101.3.4.1.44
            AESRFC3211WRAP
            ARCFOUR
            2.16.840.1.101.3.4.1.45
            ELGAMAL/NONE/NOPADDING
            RSA//OAEPPADDING
            ELGAMAL
            1.2.410.200004.7.1.1.1
            CAST6
            CAST5
            PBEWITHSHA-256AND256BITAES-CBC-BC
            2.16.840.1.101.3.4.1.41
            RSA/RAW
            PBEWITHMD5AND128BITAES-CBC-OPENSSL
            1.3.6.1.4.1.22554.1.1.2.1.2
            RSA/PKCS1
            PBEWITHSHA256AND128BITAES-CBC-BC
            RSA/ECB/PKCS1Padding
            BROKENPBEWITHMD5ANDDES
            IES
            AES SupportedKeyFormats
            RC2 SupportedKeyFormats
            Blowfish
            1.2.392.200011.61.1.1.3.4
            1.2.392.200011.61.1.1.3.3
            ELGAMAL/NONE/PKCS1PADDING
            1.2.840.113533.7.66.10
            PBEWITHSHAAND128BITRC2-CBC
            PBEWITHSHA-256AND192BITAES-CBC-BC
            PBEWITHSHA1AND40BITRC4
            AESWrap
            ELGAMAL/ECB/PKCS1PADDING
            1.3.14.3.2.7
            OID.1.2.840.113549.1.5.3
            1.2.840.113549.1.9.16.3.6
            Grainv1
            1.2.840.113549.1.9.16.3.7
            PBEWITHSHA256AND192BITAES-CBC-BC
            TEA
            Blowfish SupportedPaddings
            ELGAMAL/PKCS1
            PBEWITHSHAAND2-KEYTRIPLEDES-CBC
            RSA/1
            RSA/2
            PBEWithMD5AndTripleDES
            BrokenIES
            RSA SupportedKeyClasses
            PBEWITHSHA-1AND128BITAES-CBC-BC
            1.2.392.200011.61.1.1.3.2
            1.2.840.113549.1.1.7
            PBEWITHSHA256AND256BITAES-CBC-BC
            1.2.840.113549.1.1.1
            PBEWithSHA1AndRC2_40
            RC2
            RC4
            RC5
            RC6
            BROKENPBEWITHSHA1ANDDES
            1.2.840.113549.1.5.6
            DES
            1.2.840.113549.1.5.3
            1.2.840.113549.1.5.4
            1.2.840.113549.1.5.1
            VMPC-KSA3
            BLOWFISH
            GOST-28147
            RSA
            Twofish
            PBEWITHSHAAND3-KEYTRIPLEDES-CBC
            1.2.410.200004.1.4
            PBEWITHSHAANDTWOFISH-CBC
            HC128
            DESede SupportedKeyFormats
            1.3.6.1.4.1.22554.1.2.1.2.1.2
            RIJNDAEL
            DESede
            DES SupportedModes
            2.5.8.1.1
            SALSA20
            OLDPBEWITHSHAAND3-KEYTRIPLEDES-CBC
            PBEWITHSHA1ANDDES
            PBEWITHSHA1AND256BITAES-CBC-BC
            ECIES
            1.2.643.2.2.21
            SEED
            ARCFOUR SupportedModes
            DESEDERFC3211WRAP
            RSA//PKCS1PADDING
            RSA/OAEP
            1.3.6.1.4.1.22554.1.2.1.2.1.22
            PBEWithSHAAnd3KeyTripleDES
            1.3.6.1.4.1.3029.1.2
            PBEWITHSHAAND128BITRC4
            RC5-64
            TripleDES
            DESedeWrap SupportedModes
            PBEWITHSHA-1AND192BITAES-CBC-BC
            PBEWITHMD5ANDRC2
KeyAgreeents:
            DiffieHellman SupportedKeyClasses
            1.2.840.113549.1.3.1
            1.3.133.16.840.63.0.2
            1.3.133.16.840.63.0.16
            ECMQV
            DIFFIEHELLMAN
            OID.1.2.840.113549.1.3.1
            ECDH
            ECDHC
            DiffieHellman
            DH
Macs:
            SslMacMD5 SupportedKeyFormats
            DESEDE64WITHISO7816-4PADDING
            GOST28147MAC
            RC2MAC
            AESCMAC
            HMAC-TIGER
            HMACSHA384
            ISO9797ALG3MAC
            HmacSHA1 SupportedKeyFormats
            RC5MAC/CFB8
            HmacSHA256 SupportedKeyFormats
            HMAC-SHA256
            HMACSHA1
            DESEDECMAC
            GOST28147
            HmacPBESHA1
            1.3.6.1.5.5.8.1.3
            1.3.6.1.5.5.8.1.2
            1.3.6.1.5.5.8.1.4
            RC2/CFB8
            HMAC-SHA224
            HMACRIPEMD128
            HmacSHA384
            HmacSHA512 SupportedKeyFormats
            DESISO9797MAC
            HMACSHA512
            DESEDE64
            VMPCMAC
            HMAC-MD5
            HMAC-MD4
            HMAC-MD2
            HMAC-RIPEMD128
            ISO9797ALG3
            HMAC/SHA384
            SKIPJACKMAC/CFB8
            HMAC/MD2
            HmacPBESHA1 SupportedKeyFormats
            DESEDEISO9797ALG1MACWITHISO7816-4PADDING
            HMAC/MD4
            HMAC/MD5
            SslMacSHA1
            SKIPJACKMAC
            HMAC/SHA512
            ISO9797ALG3WITHISO7816-4PADDING
            DESMAC/CFB8
            HmacMD5 SupportedKeyFormats
            RC2MAC/CFB8
            HmacSHA1
            HMAC/RIPEMD160
            SslMacSHA1 SupportedKeyFormats
            1.3.6.1.5.5.8.1.1
            HMACTIGER
            HMAC/SHA224
            RC2
            VMPC-MAC
            RC5
            DESMAC
            HMAC-SHA384
            DES
            1.3.14.3.2.26
            PBEWITHHMACSHA
            HMAC-RIPEMD160
            PBEWITHHMACRIPEMD160
            HMACSHA256
            RC5/CFB8
            DESEDEMAC/CFB8
            HMAC/SHA1
            OLDHMACSHA512
            DESEDE/CFB8
            DESEDEMAC64WITHISO7816-4PADDING
            HmacSHA256
            DESEDE
            HMAC-SHA512
            ISO9797ALG3MACWITHISO7816-4PADDING
            HmacMD5
            HMACMD2
            HMACMD5
            HMACMD4
            SKIPJACK/CFB8
            HMACRIPEMD160
            HMACSHA224
            DESEDEMAC
            SKIPJACK
            HMAC/TIGER
            HmacSHA512
            OLDHMACSHA384
            DESEDEMAC64
            1.2.840.113549.2.10
            SslMacMD5
            HmacSHA384 SupportedKeyFormats
            DES/CFB8
            HMAC-SHA1
            VMPC
            PBEWITHHMACSHA1
            1.2.840.113549.2.7
            DESEDEISO9797ALG1WITHISO7816-4PADDING
            1.2.840.113549.2.9
            DESWITHISO9797
            1.2.840.113549.2.8
            HMAC/RIPEMD128
            1.2.840.113549.2.11
            HMAC/SHA256
            RC5MAC
MessageDigests:
            1.3.36.3.2.1
            SHA256
            Tiger
            RIPEMD320
            SHA-384
            1.3.36.3.2.3
            1.3.36.3.2.2
            RIPEMD128
            SHA ImplementedIn
            SHA
            GOST-3411
            SHA512
            SHA224
            WHIRLPOOL
            SHA1
            SHA-224
            1.3.14.3.2.26
            RIPEMD160
            MD5 ImplementedIn
            GOST
            RIPEMD256
            1.2.643.2.2.9
            GOST3411
            MD5
            MD4
            MD2
            SHA-256
            SHA-512
            1.2.840.113549.2.2
            2.16.840.1.101.3.4.2.2
            1.2.840.113549.2.5
            2.16.840.1.101.3.4.2.1
            1.2.840.113549.2.4
            2.16.840.1.101.3.4.2.4
            2.16.840.1.101.3.4.2.3
            SHA384
            SHA-1
Signatures:
            SHA256WITHRSAENCRYPTION
            RIPEMD160WITHRSAENCRYPTION
            SHA224withRSA
            SHA224withRSAandMGF1
            GOST-3410
            RIPEMD256withRSA
            SHA1withRSA/ISO9796-2
            GOST3411withGOST3410
            SHAwithDSA
            SHA512WithDSA
            MD5andSHA1withRSA
            SHA256withRSA/PSS
            GOST3410
            SHA224WITHCVC-ECDSA
            SHA1withECDSA
            MD2/RSA
            MD2WithRSAEncryption
            SHA224WITHDSA
            RIPEMD160withRSA/ISO9796-2
            SHA256/DSA
            MD5WithRSAEncryption
            RIPEMD160WithRSA
            SHA256WithDSA
            MD5/RSA
            NONEwithDSA SupportedKeyClasses
            GOST3411WITHECGOST3410
            MD5WithRSA
            SHA256withRSA SupportedKeyClasses
            SHA256/CVC-ECDSA
            SHA224WithCVC-ECDSA
            SHA224WithRSAEncryption
            MD2WithRSA
            SHA256WITHDSA
            RIPEMD256WithRSAEncryption
            SHA512WithRSAEncryption
            SHA1withDSA SupportedKeyClasses
            OID.1.2.840.10045.4.3.4
            OID.1.2.840.10045.4.3.3
            OID.1.2.840.10045.4.3.2
            OID.1.2.840.10045.4.3.1
            NONEWITHRSA
            MD5withRSA/ISO9796-2
            MD5withRSAEncryption
            SHA1withCVC-ECDSA
            SHA512WithECDSA
            SHA512withRSAEncryption
            SHA1WITHECNR
            MD2withRSA SupportedKeyClasses
            MD4withRSAEncryption
            SHA384withRSA
            ECDSAWithSHA1
            SHA1WITHRSA
            OID.1.3.36.3.3.2.2
            SHA512/DSA
            GOST-3410-94
            SHA256WITHECDSA
            SHA224withECDSA
            RIPEMD160WithECDSA
            RIPEMD160WithRSA/ISO9796-2
            1.3.14.3.2.26with1.2.840.113549.1.1.1
            1.2.840.10045.4.3.4
            MD5withRSA
            RIPEMD160WithRSAEncryption
            MD4WithRSAEncryption
            1.3.14.3.2.26with1.2.840.113549.1.1.5
            SHA224withCVC-ECDSA
            SHA224WITHECDSA
            NONEwithRSA SupportedKeyClasses
            RIPEMD160withRSA
            1.2.840.10045.4.3.2
            SHA256WithRSA
            MD5WITHRSA
            1.2.840.10045.4.3.3
            SHA1WITHRSAENCRYPTION
            1.2.840.10045.4.3.1
            SHA-1/DSA
            SHA384WithRSAEncryption
            RIPEMD160WITHECDSA
            NONEwithDSA
            SHA1WithRSAEncryption
            SHA1/RSA
            RMD160withRSA
            MD2withRSAEncryption
            GOST3411WithECGOST3410
            SHA256withCVC-ECDSA
            SHA512withECDSA
            SHA1WithDSA
            RAWRSAPSS
            RIPEMD160withRSAEncryption
            RAWDSA
            SHA224WithDSA
            SHA224WithRSA
            OID.1.2.840.113549.1.1.2
            SHA256WITHECNR
            OID.1.2.840.113549.1.1.5
            OID.1.2.840.113549.1.1.4
            SHA1withRSA SupportedKeyClasses
            SHA256WithECDSA
            SHA1withDSA KeySize
            SHA256withRSA
            SHA1/CVC-ECDSA
            RIPEMD256WithRSA
            SHA1WithECDSA
            OID.1.2.840.113549.1.1.13
            OID.1.2.840.113549.1.1.12
            OID.1.2.840.113549.1.1.11
            MD5withRSA SupportedKeyClasses
            SHA256WithRSAEncryption
            SHA384WITHECNR
            1.2.840.113549.2.5with1.2.840.113549.1.1.1
            SHA384WITHECDSA
            SHA384WithDSA
            ECDSAWITHSHA1
            OID.0.4.0.127.0.7.2.2.2.2.3
            DSA
            SHA1withRSA
            SHA224/DSA
            SHA384WITHRSAENCRYPTION
            OID.0.4.0.127.0.7.2.2.2.2.1
            RIPEMD160withECDSA
            OID.0.4.0.127.0.7.2.2.2.2.2
            SHA384withRSAandMGF1
            RIPEMD160/ECDSA
            SHA224WITHECNR
            SHA224/ECDSA
            SHA512/ECDSA
            OID.1.3.14.3.2.29
            SHA512WITHECDSA
            1.2.643.2.2.4
            1.2.643.2.2.3
            1.2.840.113549.1.1.14
            1.3.36.3.3.2.2
            1.3.36.3.3.2.1
            1.2.840.113549.1.1.13
            DSAWithSHA1
            SHA384withRSA/PSS
            OID.2.16.840.1.101.3.4.3.4
            SHA512WithRSA
            SHA256withRSAandMGF1
            1.3.36.3.3.1.4
            SHA/DSA
            1.3.36.3.3.1.2
            1.3.36.3.3.1.3
            NONEWITHRSAPSS
            SHA384WITHDSA
            1.2.840.113549.1.1.11
            DSS
            1.2.840.113549.1.1.12
            OID.2.16.840.1.101.3.4.3.1
            OID.2.16.840.1.101.3.4.3.2
            SHA224/CVC-ECDSA
            1.2.840.113549.1.1.10
            OID.2.16.840.1.101.3.4.3.3
            SHA256WITHCVC-ECDSA
            DSAwithSHA1
            RIPEMD128withRSA
            GOST3411WithGOST3410
            SHA1WITHECDSA
            SHA256withDSA
            SHA256WithCVC-ECDSA
            SHA224withRSA/PSS
            SHA1/DSA
            SHA1withDSA ImplementedIn
            SHA256withRSAEncryption
            OID.1.2.840.10040.4.3
            ECDSAwithSHA1
            NONEWITHRSASSA-PSS
            RIPEMD-160/RSA
            SHA512withDSA
            SHA1withRSAEncryption
            1.2.840.10045.4.1
            SHA512WITHDSA
            SHA1WithRSA
            GOST3411withECGOST3410
            MD4/RSA
            1.3.14.3.2.29
            RMD160WITHRSA
            SHA384withRSA SupportedKeyClasses
            SHA384/DSA
            RSAPSS
            1.2.840.113549.1.1.5
            1.2.840.113549.1.1.4
            NONEwithECDSA
            1.2.840.113549.1.1.3
            1.2.840.113549.1.1.2
            0.4.0.127.0.7.2.2.2.2.2
            NONEWITHDSA
            0.4.0.127.0.7.2.2.2.2.1
            SHA1WITHCVC-ECDSA
            SHA512withRSA SupportedKeyClasses
            RAWRSA
            ECGOST-3410
            1.3.14.3.2.27
            MD4withRSA
            SHA512withRSA/PSS
            SHA512WITHRSAENCRYPTION
            SHA384withRSAEncryption
            SHA384/ECDSA
            RSA
            SHA384WithECDSA
            SHA1WITHDSA
            0.4.0.127.0.7.2.2.2.2.3
            RMD160/RSA
            RIPEMD128WithRSAEncryption
            RAWRSASSA-PSS
            SHA1withDSA
            MD4WithRSA
            MD2WITHRSAENCRYPTION
            SHA224withDSA
            RIPEMD160WITHRSA
            RSASSA-PSS
            RIPEMD128WithRSA
            SHA1WithCVC-ECDSA
            GOST3411WITHGOST3410
            1.3.14.3.2.26with1.2.840.10040.4.3
            ECGOST3410
            SHA224withRSAEncryption
            SHA256/ECDSA
            SHA1withRSAandMGF1
            SHA384WithRSA
            ECDSA
            1.2.840.10040.4.3
            SHA-1/RSA
            SHA512withRSA
            SHA384withDSA
            SHA256withECDSA
            1.3.14.3.2.26with1.2.840.10040.4.1
            GOST-3410-2001
            SHA1withRSA/PSS
            SHA1WithRSA/ISO9796-2
            SHA224WithECDSA
            SHA512withRSAandMGF1
            SHA384withECDSA
            MD5WithRSA/ISO9796-2
            RawDSA
            MD2withRSA
            DSAWITHSHA1
            2.16.840.1.101.3.4.3.1
            SHA512WITHECNR
            2.16.840.1.101.3.4.3.3
            2.16.840.1.101.3.4.3.2
            MD5WITHRSAENCRYPTION
            2.16.840.1.101.3.4.3.4
            1.3.14.3.2.13
 */

public class ListCiphers {
	public static void printSet(String setName, Set algorithms) {
		System.out.println(setName + ":");
		if (algorithms.isEmpty()) {
			System.out.println("            None available.");
		} else {
			Iterator it = algorithms.iterator();
			while (it.hasNext()) {
				String name = (String) it.next();

				System.out.println("            " + name);
			}
		}
	}

	public static void main(String[] args) {
		Security.addProvider(new BouncyCastleProvider());
		Provider[] providers = Security.getProviders();
		Set<String> ciphers = new HashSet<String>();
		Set<String> keyAgreements = new HashSet<String>();
		Set<String> macs = new HashSet<String>();
		Set<String> messageDigests = new HashSet<String>();
		Set<String> signatures = new HashSet<String>();

		for (int i = 0; i != providers.length; i++) {
			Iterator it = providers[i].keySet().iterator();

			while (it.hasNext()) {
				String entry = (String) it.next();

				if (entry.startsWith("Alg.Alias.")) {
					entry = entry.substring("Alg.Alias.".length());
				}

				if (entry.startsWith("Cipher.")) {
					ciphers.add(entry.substring("Cipher.".length()));
				} else if (entry.startsWith("KeyAgreement.")) {
					keyAgreements
							.add(entry.substring("KeyAgreement.".length()));
				} else if (entry.startsWith("Mac.")) {
					macs.add(entry.substring("Mac.".length()));
				} else if (entry.startsWith("MessageDigest.")) {
					messageDigests.add(entry.substring("MessageDigest."
							.length()));
				} else if (entry.startsWith("Signature.")) {
					signatures.add(entry.substring("Signature.".length()));
				}
			}
		}

		printSet("Ciphers", ciphers);
		printSet("KeyAgreeents", keyAgreements);
		printSet("Macs", macs);
		printSet("MessageDigests", messageDigests);
		printSet("Signatures", signatures);
	}
}
