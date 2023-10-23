const express = require('express');
const https = require('https');
const path = require('path');
const fs = require('fs');
const queryString = require('querystring');
const port = 8081;
const { MongoClient} = require('mongodb');
// const ApiKeyManager = require('@esri/arcgis-rest-request');
const {ApiKeyManager} = require('@esri/arcgis-rest-request');
const {geocode} = require('@esri/arcgis-rest-geocoding');

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
  
  // Delete a document by ID
  app.delete('/api/userlist/:id', async (req, res) => {
    const collection = client.db('UserDB').collection('userlist');
    const result = await collection.deleteOne({ _id: ObjectID(req.params.id) });
    res.json(result);
  });
  

app.use('/', (req, res, next) => {
    res.send("Hello from SSL server")
})

const sslServer = https.createServer({
    key:fs.readFileSync(path.join(__dirname, 'certificates', 'key.pem')),
    cert:fs.readFileSync(path.join(__dirname, 'certificates', 'cert.pem'))
}, app)

/**
 * Function outputs an array of 7 entries, with each entry corresponding to when the user should be reminded to leave for class on that day.
 * Instead of array, could also directly modify the schedule.
 * Translink Open API Key: crj9j8Kj97pbPkkc61dX
 * Geocoding API key: AAPK3c726265cc41485bb57c5512e98cf912OLoJQtidjOlcqjdpa0Pl773UqNoOYfwApr6ORYd8Lina8_K0sEbdcyXsNfHFqLKE if error 498 invalid token, create new key
 * 
 * Function takes in an home/starting address and a schedule (includes location/address of classes eventually, but temporarily assume UBC for simplicity) 
 * @param {*} schedule 
 * @param {*} address 
 */
async function initReminders(schedule, address) {
    /* Get static schedule of closest bus stop to home address */
    var buses = [];
    // get closest bus stop to home address
    console.log("calling translink api");
    buses = await getNearestBuses(address);
    console.log("initReminders(): returned buses: " + buses[1].StopNo);
}
initReminders("xxx", "xxx");

function getLatLong(address) {
  return new Promise((resolve, reject) => {
    var lat = 0; // default value
    var long = 0; // default value
    var coords = [lat, long];
  
    var apiKey_geo = "AAPK3c726265cc41485bb57c5512e98cf912OLoJQtidjOlcqjdpa0Pl773UqNoOYfwApr6ORYd8Lina8_K0sEbdcyXsNfHFqLKE";
    var authentication_geo = ApiKeyManager.fromKey(apiKey_geo);
  
    console.log("getLatLong: calling geocode api");
    geocode({
      address: address,
      authentication: authentication_geo
    
    }).then(response => {
      // x: longitude, y: latitude
      console.log(response.candidates[0].location); // => { x: -77.036533, y: 38.898719, spatialReference: ... }
      lat = response.candidates[0].location.y;
      long = response.candidates[0].location.x;
      console.log("helper function getLatLong() output: " + coords + " " + lat + " " + long);
      coords[0] = lat.toFixed(5);
      coords[1] = long.toFixed(5);
      resolve(coords);
    }).catch(error => {
      console.log(error);
    });
  });
}

async function getNearestBuses(address) {
  return new Promise((resolve, reject) => {
    lat = 0;
    long = 0;
  
    console.log("--------------------");
    console.log("getBuses: calling getlatlong");
    var address = "7746 Berkley Street, Burnaby, British Columbia, Canada";
    getLatLong(address).then((coords) => {
      console.log("initReminders(): returned coords: " + coords);
      console.log("--------------------");
      lat = coords[0];
      long = coords[1];
    
      var apiKey = "crj9j8Kj97pbPkkc61dX";
    
      const queryParams = queryString.stringify({
        lat: lat,
        long: long,
        radius: 500, // Set the radius to your desired value (in meters)
      });
    
      const options = {
        hostname: 'api.translink.ca',
        path: `/rttiapi/v1/stops?apikey=${apiKey}&${queryParams}`,
        method: 'GET',
        headers: {
          'Accept': 'application/json',
        },
      };
      console.log("getnearestbuses(): options set. " + options.path);
    
      const request = https.get(options, (response) => {
        let data = '';
        console.log("getnearestbuses(): request sent");
    
        response.on('data', (chunk) => {
          data += chunk;
        });
    
        response.on('end', () => {
          console.log("getNearestBuses() return");
          console.log(JSON.parse(data));
          console.log("--------------------");
          resolve(JSON.parse(data));
        });
      }).on("error", (err) => {
          console.log("Error: " + err.message);
      });
    });
  });
}

connectToDatabase();
sslServer.listen(port, () => console.log('Secure server :) on port ' + port))
