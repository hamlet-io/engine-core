[#ftl]

[#assign candidates =
  getFileTree(
    "/products",
    {
      "Regex" : ["product.json"],
      "IgnoreDotDirectories" : false,
      "IgnoreDotFiles" : false,
	"IncludeCMDBInformation" : true	,
"UseCMDBPrefix" : true
    }
  ) ]

[#list candidates as filename,properties ]
${filename}
	[#list properties as detail,value ]
	${detail}
	[#if detail == "CMDB"]
	${value.Name} -  - ${value.BasePath} - ${value.File}
	[#else]
	${value}
	[/#if]
	[/#list]
[/#list]

