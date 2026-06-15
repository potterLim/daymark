#!/usr/bin/env bash
set -euo pipefail

line_ending_findings=()
missing_final_lf_findings=()

while IFS= read -r -d '' tracked_file; do
    if [[ ! -f "${tracked_file}" ]]; then
        continue
    fi

    mime_type="$(file -b --mime "${tracked_file}")"
    if [[ "${mime_type}" != text/* && "${mime_type}" != *xml* && "${mime_type}" != *json* ]]; then
        continue
    fi

    if LC_ALL=C grep -q $'\r' "${tracked_file}"; then
        line_ending_findings+=("${tracked_file}")
    fi

    if [[ -s "${tracked_file}" ]]; then
        last_byte="$(tail -c 1 "${tracked_file}" | od -An -t u1 | tr -d '[:space:]')"
        if [[ "${last_byte}" != "10" ]]; then
            missing_final_lf_findings+=("${tracked_file}")
        fi
    fi
done < <(git ls-files -z)

if (( ${#line_ending_findings[@]} > 0 )); then
    echo "Tracked text files with CR or CRLF line endings were found:" >&2
    printf '%s\n' "${line_ending_findings[@]}" >&2
    exit 1
fi

if (( ${#missing_final_lf_findings[@]} > 0 )); then
    echo "Tracked text files missing a final LF were found:" >&2
    printf '%s\n' "${missing_final_lf_findings[@]}" >&2
    exit 1
fi

echo "Line ending check passed."
