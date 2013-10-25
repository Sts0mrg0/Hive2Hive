package org.hive2hive.core.encryption;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;

/**
 * This class provides special encryption and decryption functionalities for files.
 * @author Christian
 *
 */
public final class FileEncryptionUtil {

	private static final H2HLogger logger = H2HLoggerFactory.getLogger(FileEncryptionUtil.class);

	private static final String DELIMITER = "DELIMITERDELIMITER";

	private FileEncryptionUtil() {
	}

	/**
	 * Create a SHA-256 checksum based on the binary representation of a file.
	 * @param filePath The path of the file which shall be check summed.
	 * @return The checksum of the file.
	 * @throws FileNotFoundException
	 */
	public static String createChecksum(Path filePath) throws FileNotFoundException {

		try {
			MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
			try {
				FileInputStream fis = new FileInputStream(filePath.toFile());

				byte[] buffer = new byte[1024];

				int numBytes = 0;
				while ((numBytes = fis.read(buffer)) != -1) {
					messageDigest.update(buffer, 0, numBytes);
				}
				fis.close();

				byte[] mdbytes = messageDigest.digest();
				StringBuffer hexString = new StringBuffer();
				for (int i = 0; i < mdbytes.length; i++) {
					hexString.append(Integer.toHexString(0xFF & mdbytes[i]));
				}
				return hexString.toString();

			} catch (IOException e) {
				logger.error("Exception while closing input stream:", e);
			}
		} catch (NoSuchAlgorithmException e) {
			logger.error("Exception during message digest initialization:", e);
		}

		return null;
	}
	
	

	public static String serializePath(Path path) {

		if (path.getNameCount() < 2) {
			return path.toString();
		} else {
			StringBuffer buffer = new StringBuffer(path.getName(0).toString());
			for (int i = 1; i < path.getNameCount(); i++) {
				buffer.append(DELIMITER);
				buffer.append(path.getName(i).toString());
			}
			return buffer.toString();
		}
	}

	public static Path deserializePath(String serializedPath) {

		String[] pathParts = serializedPath.split(DELIMITER);
		if (pathParts.length > 1) {
			String tail[] = new String[pathParts.length - 1];
			System.arraycopy(pathParts, 1, tail, 0, pathParts.length - 1);
			return Paths.get("", pathParts);
		} else if (pathParts.length == 1) {
			return Paths.get(pathParts[0]);
		}
		return null;
	}

	private static void copyStream(InputStream inputStream, OutputStream outputStream) throws IOException {

		int numBytes;
		byte[] buffer = new byte[64];
		while ((numBytes = inputStream.read(buffer)) != -1) {
			outputStream.write(buffer, 0, numBytes);
		}
		outputStream.flush();
		outputStream.close();
		inputStream.close();
	}
}
