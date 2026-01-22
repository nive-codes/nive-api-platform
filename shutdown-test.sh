#!/bin/bash
# [NOTE] SAMPLE이므로 참고 후 수정하신 뒤 사용 바랍니다.

echo "🛑 테스트 컨테이너 중지 및 삭제 중..."
docker-compose -f docker-compose.yml -f docker-compose.test.yml down

echo "✅ 컨테이너 중지 완료"
