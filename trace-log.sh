#!/bin/bash
# [NOTE] SAMPLE이므로 참고 후 수정하신 뒤 사용 바랍니다.
# 로그 내 trace_id를 토대로 json으로 뽑아서 볼 수 있도록 만든 script입니다.


if [ -z "$1" ]; then
  echo "사용법: $0 <trace_id>"
  exit 1
fi

TRACE_ID=$1
LOG_FILE="app-json.log"

grep "\"trace_id\":\"$TRACE_ID\"" "$LOG_FILE" \
| jq --color-output 'if (.stack_trace? and (.stack_trace | type == "string")) then
         .stack_trace |= gsub("\\t"; "    ") |
         .stack_trace |= gsub("\\n"; "___LINE_BREAK___")
     else
         .
     end' \
| sed 's/___LINE_BREAK___/\n/g'
