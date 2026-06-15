#!/usr/bin/env bash
set -euo pipefail

FORBIDDEN_PATH_PATTERN='(^|/)(deployment|operations-handoff|release-readiness|ecs-express-migration)\.md$|(^|/)deploy-production\.yml$|(^|/)push-seoul-image\.yml$|(^|/)app-runner-env\.example$|(^|/)daymark-(sign-in|create-account)\.png$|(^|/)\.env$|.*\.(pem|p12|pfx|jks|keystore|sqlite|sqlite3|db|dump|bak|backup|log)$'
FORBIDDEN_CONTENT_PATTERN='/Users/potterlim|/var/folders|TemporaryItems|NSIRD|KakaoTalk|스크린샷|daily-log-screenshots|daymark-final-qa|104531737396|AKIA[0-9A-Z]{16}|ASIA[0-9A-Z]{16}|GOCSPX-|AIza[0-9A-Za-z_-]{20,}|ghp_[A-Za-z0-9_]{20,}|github_pat_[A-Za-z0-9_]{20,}|BEGIN (RSA |EC |OPENSSH )?PRIVATE KEY|AWS_SECRET_ACCESS_KEY|aws_secret_access_key'

TRACKED_PATH_FINDINGS="$(git ls-files | grep -E "${FORBIDDEN_PATH_PATTERN}" || true)"
if [[ -n "${TRACKED_PATH_FINDINGS}" ]]; then
    echo "Tracked local-only or sensitive-looking paths were found:" >&2
    echo "${TRACKED_PATH_FINDINGS}" >&2
    exit 1
fi

if git grep -n -I -E "${FORBIDDEN_CONTENT_PATTERN}" -- . ':!scripts/verify-public-repository.sh'; then
    echo "Tracked files contain local-only paths or secret-like patterns." >&2
    exit 1
fi

echo "Public repository hygiene check passed."
