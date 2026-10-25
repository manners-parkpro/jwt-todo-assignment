"""InstructionsLoaded 훅: CLAUDE.md·.claude/rules 파일이 언제, 왜 로드됐는지 .claude/instructions-log.jsonl에 남긴다.

세션 시작(session_start)·압축 뒤(compact)·경로 규칙 일치(path_glob_match) 때 지침이 실제로 로드됐는지
확인하는 근거다. 로드 시점 파일의 글자 수와 해시를 함께 남겨 현재 파일과 비교할 수 있게 한다.
이 이벤트는 비동기로 실행되고 출력이 무시되므로 컨텍스트에는 아무것도 넣지 않는다.
기록 파일은 공개 저장소와 제출 ZIP에서 제외한다. 훅 설정은 로컬 전용 .claude/settings.local.json에 있다.
"""
import datetime
import hashlib
import json
import os
import sys
from pathlib import Path

CLAUDE_DIR = Path(__file__).resolve().parent.parent
PROJECT_DIR = CLAUDE_DIR.parent
LOG_PATH = CLAUDE_DIR / "instructions-log.jsonl"


def relative(path):
    if not path:
        return None
    try:
        return str(Path(path).resolve().relative_to(PROJECT_DIR))
    except ValueError:
        return path


def main() -> None:
    event = json.load(sys.stdin)
    content = Path(event["file_path"]).read_bytes()
    record = {
        "timestamp": datetime.datetime.now().astimezone().isoformat(timespec="seconds"),
        "session_id": event.get("session_id"),
        "load_reason": event.get("load_reason"),
        "file": relative(event["file_path"]),
        "chars": len(content.decode("utf-8")),
        "sha256": hashlib.sha256(content).hexdigest()[:12],
        "trigger": relative(event.get("trigger_file_path")),
    }
    fd = os.open(LOG_PATH, os.O_WRONLY | os.O_APPEND | os.O_CREAT, 0o600)
    with os.fdopen(fd, "a", encoding="utf-8") as log:
        log.write(json.dumps(record, ensure_ascii=False) + "\n")


if __name__ == "__main__":
    try:
        main()
    except Exception as error:
        print(f"instructions-log 기록 실패: {error}", file=sys.stderr)
        sys.exit(1)
