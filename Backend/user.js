
var express = require("express")
var app = express()

const {MongoClient, MongoRuntimeError} = require("mongodb")
const uri = "mongodb://0.0.0.0:27017"
const client = new MongoClient(uri)

app.use(express.json())

app.get("/", (req, res) => {
    res.send("Hello world!")
})

app.post("/", (req, res) => {
    res.send(req.body.text)
})

app.post("/userlist", async (req, res) => {
    try {
        await client.db("test").collection("userlist").insertOne(req.body)
        res.status(200).send("user item added successfully\n")
    } catch(err) {
        console.log(err)
        res.status(400).send(err)
    }
    
})

app.get("/userlist", async (req, res) => {
    try{
        const result = await client.db("test").collection("userlist").find(req.body).toArray()
        res.send(result)
    }
    catch(err){
        console.log(err)
        res.status(400).send(err)
    }
    })

app.put("/userlist", async (req, res) => {
    try{
        await client.db("test").collection("userlist").replaceOne({"task": "Finish this tutorial"}, req.body)
        res.status(200).send("Todo item modified successfully\n")
    }
    catch(err){
        console.log(err)
        res.status(400).send(err)
    }
})

app.delete("/userlist", async (req, res) => {
    try{
        await client.db("test").collection("userlist").deleteOne({"task":req.body.task})
        res.status(200).send("user item deleted successfully\n")
    }
    catch(err){
        console.log(err)
        res.status(400).send(err)
    }
    })

    

async function run() {
    try{
        await client.connect()
        console.log("Successfully connected to the database")
        var server = app.listen(8081, (req, res) => {
            var host = server.address().address
            var port = server.address().port
            console.log("Example server successfully running at http://%s:%s", host, port)
        })
    }catch(err){
        console.log(err)
        await client.close()

    }
}

run()