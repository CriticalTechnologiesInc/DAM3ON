package sandbox;


public class tmp {

	public static void main(String[] args) {
		String sa = "1cba565aa4c9f63cc22b993a9ba88ab2e8eb35a8";
		
		String h1 = "374df527d4a42c9667f62e9119b0956f64d20102";
		String h1a = "root=UUID=fc89991a-9412-40e6-9ac2-6b900ed3d1d2 ro intel_iommu=on"; //b0f0930e2c823d3c6f4720d8001a4bd168617147
//		String h1ah = "b0f0930e2c823d3c6f4720d8001a4bd168617147";
		String h2 ="8c2a8ed5fba2353cda0c6e8fc00d80c93dba2744";
		
		String h1ah = core.Utils.byteArrayToHex(core.Utils.byteSha(h1a.getBytes()));
		
		String target = "5cba16fec78794c7631ead5161f20e927330fb9c";
		
		byte[] hash = null;
		
		hash = core.Utils.byteSha(core.Utils.concatBytes(new byte[20], core.Utils.hexToBin(sa)));
		hash = core.Utils.byteSha(core.Utils.concatBytes(hash, core.Utils.hexToBin(h1)));
		hash = core.Utils.byteSha(core.Utils.concatBytes(hash, core.Utils.hexToBin(h1ah)));
		hash = core.Utils.byteSha(core.Utils.concatBytes(hash, core.Utils.hexToBin(h2)));
		
		System.out.println("actual: " + core.Utils.byteArrayToHex(hash));
		System.out.println("target: " + target);
	}


}
