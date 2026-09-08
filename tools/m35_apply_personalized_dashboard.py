from pathlib import Path

path = Path('app/src/main/java/com/whoareyou/app/RetentionUi.kt')
text = path.read_text(encoding='utf-8')

needle = '''    val quizCatalog = remember(context) { QuizRepository.load(context) }\n    val totalQuizCount = quizCatalog.size\n'''
replacement = '''    val quizCatalog = remember(context) { QuizRepository.load(context) }\n    val personalizedProfile = remember(quizCatalog, storedProfile.latestScores, storedProfile.previousScores) {\n        GlobalProfileEngine.build(quizCatalog, storedProfile.latestScores, storedProfile.previousScores)\n    }\n    val totalQuizCount = quizCatalog.size\n'''
if needle not in text:
    raise SystemExit('Profile insertion point not found; refusing unsafe patch')
text = text.replace(needle, replacement, 1)

needle2 = '''    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {\n        JourneyPulse(\n'''
replacement2 = '''    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {\n        PersonalizedDiscoverDashboard(\n            profile = personalizedProfile,\n            quizzes = quizCatalog,\n            completed = storedProfile.completedQuizIds\n        )\n        JourneyPulse(\n'''
if needle2 not in text:
    raise SystemExit('Dashboard insertion point not found; refusing unsafe patch')
text = text.replace(needle2, replacement2, 1)

path.write_text(text, encoding='utf-8')
