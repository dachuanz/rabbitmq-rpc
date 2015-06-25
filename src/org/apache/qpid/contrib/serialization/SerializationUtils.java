package org.apache.qpid.contrib.serialization;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.caucho.hessian.io.Deflation;
import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;

/**
 * 
 * @author 
 *
 */
public class SerializationUtils {
	/**
	 * Hessian2 数据序列化
	 * @param source
	 * @return
	 */
	public static byte[] writeObject(Object source) {
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			Hessian2Output out = new Hessian2Output(bos);
			// out.startMessage();
			out.writeObject(source);
			// out.completeMessage();
			out.flush();
			return bos.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 
	 * @param bytes
	 * @return
	 */
	public Object readObject(byte[] bytes) {
		try {
			ByteArrayInputStream bin = new ByteArrayInputStream(bytes);
			Hessian2Input in = new Hessian2Input(bin);
			// in.startMessage();
			Object obj = in.readObject();
			// in.completeMessage();
			return obj;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	// hessian 2 with deflat
	static final Deflation envelope = new Deflation();

	/**
	 * 使用压缩
	 * @param source
	 * @return
	 */
	public static byte[] writeObjectWithZip(Object source) {
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			Hessian2Output out = new Hessian2Output(bos);
			out = envelope.wrap(out);
			out.writeObject(source);
			out.flush();
			out.close(); // 记得关闭
			return bos.toByteArray();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 
	 * 使用压缩
	 * @param bytes
	 * @return
	 */
	public static Object readObjectWithZip(byte[] bytes) {
		try {
			ByteArrayInputStream bin = new ByteArrayInputStream(bytes);
			Hessian2Input in = new Hessian2Input(bin);
			in = envelope.unwrap(in);
			Object obj = in.readObject();
			in.close();
			return obj;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

}
