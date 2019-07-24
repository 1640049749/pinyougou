<html>
<head>
    <meta charset="UTF-8">
    <#--引入模板文件-->
    <#include "header.ftl"  />
</head>
<body>
<#--${name} 插值表达式-->

<#--<#assign 变量名="zhangsan" /> 设置变量的值-->
<#assign myname="zhangsan" />
<#assign myperson={"id":1,"name":"zhangsan"} />
<#assign myperson123='{"id":1,"name":"zhangsan"}' />
<#assign flag=false />

<h1 style="color: red">${name}</h1>
<h1 style="color: red">${myname}</h1>

<br>
<#--获取对象数据类型的值-->

id:${user.id?c}
name:${user.name}
<br>
数据1：${myperson.id}
数据2：${myperson.name}
<#if 1==1>
    <div>
        数据1
    </div>
<#else >
    <div>
        数据2
    </div>
</#if>

<table>
    <tr>
        <td>id</td>
        <td>name</td>
        <td>index</td>
    </tr>
    <#list mylist as item>
        <tr >
            <td>${item.id}</td>
            <td>${item.name}</td>
            <td>${item_index+1}</td>
        </tr>
    </#list>
</table>

总记录数是：${mylist?size}

<br>
获取的数据：
${myperson123}

<#assign mypersonjson=myperson123?eval />
<br>
获取值：${mypersonjson.id}<br>
获取值：${mypersonjson.name}
<br>
获取日期：
日期
只有日期${date?date}<br>
只有时间${date?time}<br>
都有：${date?datetime}<br>
自定义格式：${date?string("yyyy/MM/dd HH:mm:ss")}<br>
名人：
${nullkey!}

<br>
<br>
<#if nullkey??>
    ${nullkey}
<#else>
    没有值
</#if>


<br>
<#if (1 > 2) >
我就是显示1
<#else>
如果1》2 xiansih2
</#if>




</body>
</html>

