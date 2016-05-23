# RuleMiner
###### RuleMiner
<br>
参数示例：
<br>
yago2core.10kseedsSample.compressed.notypes.tsv -maxad 3 -numThread 4 -const 5 -open 20
<br>
分别表示：
<br>
文件名
<br>
规则最大长度
<br>
线程数
<br>
InstantiatedAtoms开关
<br>
InstantiatedAtoms threshold倍数
<br>
非封闭规则开关
<br>
非封闭规则threshold倍数
<br>
<br>
加-open参数速度变慢很多，继续优化中。
<br>
加-open参数速度变慢很多，是由于计算PCA Confidence的时间过长，这里我们在计算open atoms时，使用STD Confidence。
<br>
非封闭规则数量太多，加上threshold倍数限制。
<br>
非封闭规则std body size 计算方式修改。
<br>
