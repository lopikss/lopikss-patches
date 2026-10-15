import importlib.util
import io
import tempfile
import unittest
import zipfile
from pathlib import Path

MODULE_PATH = Path(__file__).resolve().parents[1] / "tools" / "inspect_apks.py"
spec = importlib.util.spec_from_file_location("inspect_apks", MODULE_PATH)
inspector = importlib.util.module_from_spec(spec)
spec.loader.exec_module(inspector)


class ScannerTest(unittest.TestCase):
    def test_apk_marker_detection(self):
        with tempfile.TemporaryDirectory() as directory:
            p = Path(directory) / "test.apk"
            with zipfile.ZipFile(p, "w") as z:
                z.writestr("classes.dex", b"billing@@8.3.0|BUY_INTENT")
                z.writestr("lib/arm64-v8a/libil2cpp.so", b"nativecode")
            result = inspector.scan(p)
            self.assertTrue(result["markers"]["billing_8_3_0"])
            self.assertTrue(result["markers"]["billing_buy_intent"])
            self.assertFalse(result["markers"]["billing_activity_result"])

    def test_nested_xapk_and_comparison(self):
        with tempfile.TemporaryDirectory() as directory:
            base = io.BytesIO()
            with zipfile.ZipFile(base, "w") as z:
                z.writestr("classes.dex", b"original")
                z.writestr("lib/arm64-v8a/libil2cpp.so", b"SAME")
            xapk = Path(directory) / "original.xapk"
            with zipfile.ZipFile(xapk, "w") as z:
                z.writestr("io.supercent.weaponrpg.apk", base.getvalue())
            patched = Path(directory) / "patched.apk"
            with zipfile.ZipFile(patched, "w") as z:
                z.writestr("classes.dex", b"changed")
                z.writestr("lib/arm64-v8a/libil2cpp.so", b"SAME")
            diff = inspector.compare(inspector.scan(xapk), inspector.scan(patched))
            self.assertEqual(diff["changed_dex"], ["classes.dex"])
            self.assertEqual(diff["changed_unity_files"], [])
            self.assertIn("lib/arm64-v8a/libil2cpp.so", diff["unchanged_unity_files"])


if __name__ == "__main__":
    unittest.main()
