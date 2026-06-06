#!/bin/bash

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

echo "========================================"
echo "  手作作品展示分享空间 - 停止服务"
echo "========================================"
echo ""

cd "$PROJECT_ROOT"
docker compose down

echo ""
echo "✅ 服务已停止"
echo ""
echo "========================================"
