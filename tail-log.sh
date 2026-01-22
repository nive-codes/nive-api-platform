#!/bin/bash
# [NOTE] SAMPLE이므로 참고 후 수정하신 뒤 사용 바랍니다.
tail -F app-json.log \
| jq --unbuffered 'if (.stack_trace? and (.stack_trace | type == "string")) then
    .stack_trace |= gsub("\\t"; "    ") |
    .stack_trace |= gsub("\\n"; "___LINE_BREAK___")
else
    .
end' \
| sed --unbuffered 's/___LINE_BREAK___/\n/g'