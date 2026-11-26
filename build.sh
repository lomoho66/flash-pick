#!/bin/bash
set -e
export JAVA_HOME=$HOME/jdk17
export PATH=$JAVA_HOME/bin:$PATH
BT=$HOME/android-sdk/build-tools/34.0.0
AJ=$HOME/android-sdk/platforms/android-34/android.jar
ROOT=$(cd "$(dirname "$0")" && pwd)
APP=$ROOT/app
B=$APP/build
rm -rf $B && mkdir -p $B/compiled $B/gen $B/classes $B/dex

echo "[1/6] aapt2 compile"
$BT/aapt2 compile --dir $APP/res -o $B/compiled/res.zip

echo "[2/6] aapt2 link"
$BT/aapt2 link -o $B/app-unsigned.apk -I $AJ --manifest $APP/AndroidManifest.xml --custom-package com.lomoho.quickpick --java $B/gen $B/compiled/res.zip

echo "[3/6] javac"
find $APP/src $B/gen -name '*.java' > $B/sources.txt
# 注意：javac 失败必须让整个构建失败（set -e），不要用管道 + || true 吞掉编译错误，
# 否则会产出缺类的残包。
javac -source 8 -target 8 -encoding UTF-8 -cp $AJ -d $B/classes @$B/sources.txt

echo "[4/6] r8"
(cd $B/classes && jar cf $B/classes.jar .)
# R8 压缩+混淆（规则见 app/r8-rules.txt：清单组件保名，内部类任压）
cat $APP/r8-rules.txt > $B/r8-rules-full.txt
echo "-printmapping $B/mapping.txt" >> $B/r8-rules-full.txt
java -cp $BT/lib/d8.jar com.android.tools.r8.R8 --release --min-api 26 --lib $AJ --pg-conf $B/r8-rules-full.txt --output $B/dex $B/classes.jar
# 出厂自检：dex 必须含入口 Activity，缺了直接判构建失败，不许出包
for C in MainActivity SettingsActivity AboutActivity JumpActivity WidgetDuo WidgetPddPick WidgetPddCode WidgetCnPick WidgetCnCode PinCallbackReceiver; do
  $BT/dexdump $B/dex/classes.dex 2>/dev/null | grep -q "Lcom/lomoho/quickpick/$C;" || { echo "FATAL: dex missing $C"; exit 1; }
done

echo "[5/6] pack + zipalign"
cp $B/app-unsigned.apk $B/app-with-dex.apk
(cd $B/dex && zip -9 -q $B/app-with-dex.apk classes.dex)
$BT/zipalign -f -p 4 $B/app-with-dex.apk $B/app-aligned.apk

echo "[6/6] sign"
KS=$ROOT/release.keystore
if [ ! -f $KS ]; then
  PASS=$(head -c 18 /dev/urandom | base64 | tr -d '/+=' | head -c 20)
  echo "$PASS" > $ROOT/keystore-password.txt
  keytool -genkeypair -keystore $KS -alias flashpick -keyalg RSA -keysize 2048 -validity 10000 -storepass "$PASS" -keypass "$PASS" -dname "CN=lomoho, O=personal, C=CN" 2>/dev/null
fi
PASS=$(cat $ROOT/keystore-password.txt)
OUT=$ROOT/flashpick-v1.0.0.apk
$BT/apksigner sign --v1-signing-enabled false --ks $KS --ks-pass pass:"$PASS" --key-pass pass:"$PASS" --out $OUT $B/app-aligned.apk
$BT/apksigner verify --print-certs $OUT | head -4
mkdir -p $ROOT/mappings
cp $B/mapping.txt $ROOT/mappings/mapping-v$(grep -o 'versionName="[^"]*"' $APP/AndroidManifest.xml | cut -d'"' -f2).txt
ls -la $OUT
echo BUILD_OK
