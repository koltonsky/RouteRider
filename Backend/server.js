const express = require('express')
const https = require('https')
const path = require('path')
const fs = require('fs')
const port = 8081
const { MongoClient} = require('mongodb');

const app = express()
app.use(express.json());


// MongoDB connection setup
const uri = 'mongodb://0.0.0.0:27017'; // Replace with your MongoDB connection string
const client = new MongoClient(uri);


async function connectToDatabase() {
    try {
        await new Promise(resolve => setTimeout(resolve, port));
        await client.connect();
        console.log('Connected to MongoDB');
    } catch (error) {
        console.error('MongoDB Connection Error:', error);
    }
}

// Get all documents from a collection
app.get('/api/userlist', async (req, res) => {
    const collection = client.db('UserDB').collection('userlist');
    const documents = await collection.find({}).toArray();
    res.json(documents);
  });
  
// Get a single document by ID
app.get('/api/userlist/:id', async (req, res) => {
    const collection = client.db('UserDB').collection('userlist');
    const document = await collection.findOne({ _id: ObjectID(req.params.id) });
    res.json(document);
  });
  
// Create a new document
app.post('/api/userlist', async (req, res) => {
    const collection = client.db('UserDB').collection('userlist');
    const result = await collection.insertOne(req.body);
    res.json(result);
  });
  
  // Update a document by ID
app.put('/api/userlist/:id', async (req, res) => {
    const collection = client.db('UserDB').collection('userlist');
    const updatedDocument = { $set: req.body };
    const result = await collection.updateOne({ _id: ObjectID(req.params.id) }, updatedDocument);
    res.json(result);
  });
  
// Get a person's schedule by ID
app.get('/api/schedulelist/:id', async (req, res) => {
  const collection = client.db('ScheduleDB').collection('schedulelist');
  const schedule = await collection.findOne({ _id: ObjectID(req.params.id) });
  res.json(schedule);
});

// Create a person's schedule
app.post('/api/schedulelist', async (req, res) => {
  const collection = client.db('ScheduleDB').collection('schedulelist');
  const result = await collection.insertOne(req.body);
  res.json(result);
});

// Update a person's schedule by ID
app.put('/api/schedulelist/:id', async (req, res) => {
  const collection = client.db('ScheduleDB').collection('schedulelist');
  const updatedSchedule = { $set: req.body };
  const result = await collection.updateOne({ _id: ObjectID(req.params.id) }, updatedSchedule);
  res.json(result);
});
  

app.use('/', (req, res, next) => {
    res.send("Hello from SSL server")
})

const sslServer = https.createServer({
    key:fs.readFileSync(path.join(__dirname, 'certificates', 'key.pem')),
    cert:fs.readFileSync(path.join(__dirname, 'certificates', 'cert.pem'))
}, app)


connectToDatabase();
sslServer.listen(port, () => console.log('Secure server :) on port ' + port))
