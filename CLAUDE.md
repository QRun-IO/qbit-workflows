# qbit-workflows

## Knowledge base

Reviewed dossiers for this repo and the wider QQQ platform live in the second-brain
vault:

- Hub / start here: `R:/Git.Local/KofTwentyTwo/second-brain/knowledge/qqq/qqq-hub.md`
- This repo's dossier:
  `R:/Git.Local/KofTwentyTwo/second-brain/knowledge/qqq/repos/qbit-workflows.md`
  (reviewed at main `4f3c7719f9f9`, pom 0.4.0, 2026-07-04)
- QBit mechanics refresher:
  `R:/Git.Local/KofTwentyTwo/second-brain/knowledge/qqq/architecture/metadata-model.md`

Key facts from the review (see dossier for detail):

- develop carries unreleased 2026 feature work plus everything on main, and builds on
  qqq 4.0.0 via parent `com.kingsrook:qbit-build-parent:2.0.0` (`-Pqqq-snapshot`
  checks qqq 4.1.0-SNAPSHOT); main is the released line (0.4.0) on qqq 0.35.0 via
  parent 1.5.1.
- README and CHANGELOG.md do not describe this codebase (template rot) — trust the
  source and the dossier instead.
- Current first-party license declarations use Apache-2.0 consistently across
  LICENSE/NOTICE, the pom, source headers, Checkstyle template and README.
