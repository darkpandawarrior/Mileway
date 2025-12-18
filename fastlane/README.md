# Fastlane

| Lane | Command | Purpose |
|------|---------|---------|
| `test` | `bundle exec fastlane test` | Run unit tests |
| `build` | `bundle exec fastlane build` | Assemble debug APK |
| `ci` | `bundle exec fastlane ci` | Tests → build (CI gate) |
| `screenshots` | `bundle exec fastlane screenshots` | Record Roborazzi baselines |
| `verify_screenshots` | `bundle exec fastlane verify_screenshots` | Diff screenshots vs baselines |
| `release_bundle` | `bundle exec fastlane release_bundle` | Build release AAB |

Run `bundle install` once before using any lane.
