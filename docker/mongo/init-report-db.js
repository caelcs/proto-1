db = db.getSiblingDB('report');
db.createUser(
  {
    user: "report",
    pwd: "mypassword",
    roles: [ { role: "readWrite", db: "report" } ]
  }
)