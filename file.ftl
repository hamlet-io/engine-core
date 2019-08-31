[#ftl]

[#assign regex=["product.json"]]
[#assign candidates =
  getFileTree(
    "/products",
    {
        "Regex" : regex,
        "IgnoreDotDirectories" : false,
        "IgnoreDotFiles" : false,
	"IncludeCMDBInformation" : true	,
	"UseCMDBPrefix" : false
    }
  ) ]

[#list candidates as filename,properties ]
${filename}
	[#list properties as detail,value ]
	${detail}
	[#if detail == "CMDB"]
	${value.Name} -  - ${value.BasePath} - ${value.File}
	[#elseif detail == "ContentsAsJSON"]
	${value.Product.Id}
	[#else]
	${value}
	[/#if]
	[/#list]
[/#list]

