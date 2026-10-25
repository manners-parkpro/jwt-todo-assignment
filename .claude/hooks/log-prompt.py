"""UserPromptSubmit 훅: 제출된 프롬프트 원문을 .claude/prompt-history.jsonl에 한 줄씩 덧붙인다.

컨텍스트 초기화·압축이나 Claude Code의 트랜스크립트·붙여넣기 캐시 자동 정리(cleanupPeriodDays)와
무관하게 프롬프트 원문을 로컬에 남기는 백업이다. 기록 파일은 공개 저장소와 제출 ZIP에서 제외한다.
훅 설정은 로컬 전용 .claude/settings.local.json에 있다.

stdout에는 아무것도 쓰지 않는다(UserPromptSubmit의 stdout은 Claude 컨텍스트에 추가된다).
종료 코드 2는 프롬프트를 막으므로 쓰지 않는다. 실패하면 1로 끝내 경고만 표시되게 한다.
"""
import datetime
import json
import os
import sys
from pathlib import Path

LOG_PATH = Path(__file__).resolve().parent.parent / "prompt-history.jsonl"
# 백그라운드 작업 완료 알림과 서브에이전트 보고도 이 이벤트로 들어오지만 사용자 프롬프트가 아니므로 기록하지 않는다
HARNESS_MESSAGE_PREFIXES = ("<task-notification>", "<agent-message")


def main() -> None:
    event = json.load(sys.stdin)
    prompt = event.get("prompt", "")
    if prompt.startswith(HARNESS_MESSAGE_PREFIXES):
        return
    record = {
        "timestamp": datetime.datetime.now().astimezone().isoformat(timespec="seconds"),
        "session_id": event.get("session_id"),
        "source": "hook",
        "prompt": prompt,
    }
    fd = os.open(LOG_PATH, os.O_WRONLY | os.O_APPEND | os.O_CREAT, 0o600)
    with os.fdopen(fd, "a", encoding="utf-8") as log:
        log.write(json.dumps(record, ensure_ascii=False) + "\n")


if __name__ == "__main__":
    try:
        main()
    except Exception as error:
        print(f"prompt-history 기록 실패: {error}", file=sys.stderr)
        sys.exit(1)
