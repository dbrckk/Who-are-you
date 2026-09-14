from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]

class ShareRenderingPerformanceContractTest(unittest.TestCase):
    def setUp(self):
        self.result = (ROOT / 'app/src/main/java/com/whoareyou/app/ResultShare.kt').read_text(encoding='utf-8')
        self.match = (ROOT / 'app/src/main/java/com/whoareyou/app/CompatibilityShare.kt').read_text(encoding='utf-8')
        self.profile = (ROOT / 'app/src/main/java/com/whoareyou/app/GlobalProfileShare.kt').read_text(encoding='utf-8')

    def test_cpu_rendering_uses_default_dispatcher(self):
        for source in (self.result, self.match, self.profile):
            self.assertIn('withContext(Dispatchers.Default)', source)

    def test_file_work_uses_io_dispatcher(self):
        for source in (self.result, self.match, self.profile):
            self.assertIn('withContext(Dispatchers.IO)', source)
            self.assertIn('ShareFileStore.writePng(', source)

    def test_result_share_uses_known_quiz_id_instead_of_catalog_parse(self):
        self.assertIn('quizId: String', self.result)
        self.assertNotIn('QuizRepository.load(', self.result)

if __name__ == '__main__':
    unittest.main()
