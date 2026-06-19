#!/usr/bin/env bash
#
# Applies branch protection to `main` and `develop` via the GitHub API.
#
# Requirements:
#   * `gh` authenticated as a repository admin.
#   * For a PRIVATE repository, a GitHub Pro/Team/Enterprise plan — branch
#     protection and rulesets on private repos are paid features. PUBLIC
#     repositories get branch protection for free.
#
# Policy applied:
#   main    — PR required (1 approval, stale reviews dismissed), all CI matrix
#             checks must pass, linear history, no force-push, no deletion,
#             conversations must be resolved.
#   develop — all CI matrix checks must pass, no force-push, no deletion; the
#             maintainer may push directly (enforce_admins is false).
#
# Usage:  scripts/apply-branch-protection.sh [owner/repo]
#
set -euo pipefail

REPO="${1:-DemchaAV/graphcompose-markdown}"
CHECKS='["Build and run tests (JDK 17)", "Build and run tests (JDK 21)", "Build and run tests (JDK 25)"]'

echo "Protecting main on ${REPO} ..."
gh api -X PUT "repos/${REPO}/branches/main/protection" --input - <<JSON
{
  "required_status_checks": { "strict": true, "contexts": ${CHECKS} },
  "enforce_admins": false,
  "required_pull_request_reviews": {
    "dismiss_stale_reviews": true,
    "required_approving_review_count": 1
  },
  "restrictions": null,
  "required_linear_history": true,
  "allow_force_pushes": false,
  "allow_deletions": false,
  "required_conversation_resolution": true
}
JSON

echo "Protecting develop on ${REPO} ..."
gh api -X PUT "repos/${REPO}/branches/develop/protection" --input - <<JSON
{
  "required_status_checks": { "strict": false, "contexts": ${CHECKS} },
  "enforce_admins": false,
  "required_pull_request_reviews": null,
  "restrictions": null,
  "required_linear_history": false,
  "allow_force_pushes": false,
  "allow_deletions": false
}
JSON

echo "Done. Branch protection applied to main and develop on ${REPO}."
