#!/usr/bin/env bash
# CAIRO logo 全尺寸导出:权威源 public/logo/logo.svg → 各常见尺寸 PNG + 顶层兼容分发。
# 再设计流程:只改 logo/logo.svg → 跑本脚本,所有尺寸与引用位一次更新。
# 依赖:macOS + Google Chrome(headless 渲染,透明底)。
# 关键:Chrome 直接打开无 width/height 的 SVG 不铺满视口(默认 intrinsic 尺寸,四周留白),
# 必须用临时 HTML 包装把 <img> 强制到目标尺寸,logo 才能完整占满导出画布。
set -euo pipefail

DIR="$(cd "$(dirname "$0")/.." && pwd)/public/logo"
SRC="$DIR/logo.svg"
CHROME="/Applications/Google Chrome.app/Contents/MacOS/Google Chrome"
[ -x "$CHROME" ] || { echo "Chrome 未找到: $CHROME"; exit 1; }

render() { # render <size> <output>
  local s="$1" out="$2" html="$DIR/.render.html"
  cat > "$html" <<EOF
<!doctype html>
<meta charset="utf-8">
<style>
  html, body { margin: 0; padding: 0; width: ${s}px; height: ${s}px; overflow: hidden; background: transparent; }
  img { width: ${s}px; height: ${s}px; display: block; }
</style>
<img src="logo.svg">
EOF
  rm -f "$out"
  "$CHROME" --headless --disable-gpu --no-sandbox \
    --screenshot="$out" --window-size="$s,$s" \
    --default-background-color=00000000 \
    "file://$html" >/dev/null 2>&1
  rm -f "$html"
  [ -f "$out" ] || { echo "导出失败: $out"; exit 1; }
  echo "✓ ${out##*/} ($(${CHROME%%/MacOS*} 2>/dev/null; echo)$s x $s)"
}

# 常见尺寸清单:favicon 系 / 触屏 / PWA / 通用
SIZES=(16 32 48 64 128 180 192 512)
for s in "${SIZES[@]}"; do
  render "$s" "$DIR/logo-$s.png"
done

# 顶层兼容分发(历史引用路径零改动)
cp "$DIR/logo.svg" "$DIR/../logo.svg"
cp "$DIR/logo-32.png" "$DIR/../favicon.png"
cp "$DIR/logo-180.png" "$DIR/../apple-touch-icon.png"
cp "$DIR/logo-180.png" "$DIR/../appicon-apple.png"
echo "✓ 顶层分发: logo.svg / favicon.png(32) / apple-touch-icon.png(180) / appicon-apple.png(180)"
echo "完成。再设计只需替换 $SRC 后重跑。"
