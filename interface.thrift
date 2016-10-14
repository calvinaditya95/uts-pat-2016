namespace java Service

struct Data {
	1: optional string name;
	2: required i64 size;
	3: required i64 lastModifiedDate;
	4: required i64 createdDate;
	5: optional binary buffer;
}

service Service {
	list<Data> dir(1:string path),
	string createDir(1:string path, 2:string name),
	Data getFile(1:string path, 2:string name),
	string putFile(1:string path, 2:string name, 3:Data data)
}