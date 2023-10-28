const express = require('express');
const https = require('https');
const path = require('path');
const fs = require('fs');
const queryString = require('querystring');
const port = 8081;
// const fetch = require('node-fetch');
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

/**
 * Function outputs an array of 7 entries, with each entry corresponding to when the user should be reminded to leave for class on that day.
 * Instead of array, could also directly modify the schedule.
 * Translink Open API Key: crj9j8Kj97pbPkkc61dX
 * Geocoding API key: AAPK3c726265cc41485bb57c5512e98cf912OLoJQtidjOlcqjdpa0Pl773UqNoOYfwApr6ORYd8Lina8_K0sEbdcyXsNfHFqLKE if error 498 invalid token, create a new key
 * HERE Location services API key: S3186X1u-4DFckek542dcP9gxZeLI3uHQl_IkwZnJb4
 *                        App ID:  cOIE7nteY1IGtsu8BGpr
 * Google Direction API ($200 credit): AIzaSyBVsUyKxBvRjE0XdooMuoDrpAfu1KO_2mM 
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
    console.log("initReminders(): returned buses: " + buses[1].StopNo + " " + buses.length);

     
}
// initReminders("xxx", "xxx");
planTransitTrip('5870 Rumble Street, Burnaby, BC', "UBC Exchange Bus Loop", new Date("2023-10-28T18:00:00.000Z"));

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
    var address = "Student Union Boulevard, Vancouver, British Columbia, Canada";
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

async function planTransitTrip(origin, destination, arriveTime) {
  return new Promise((resolve, reject) => {
    const apiUrl = 'https://maps.googleapis.com/maps/api/directions/json';
    const apiKey = 'AIzaSyBVsUyKxBvRjE0XdooMuoDrpAfu1KO_2mM';
    // var origin = '7746 Berkley Street, Burnaby, BC'; // joyce collingwood
    // var destination = 'UBC Exchange Bus Loop'; // ubc exhcange r4
  
    // determine whether to take 99 B-Line or R4 based on address
    var addressCoords = getLatLong(origin);
    var distToCommercial = calcDist(addressCoords[0], addressCoords[1], 49.2624, -123.0698);
    var distToJoyce = calcDist(addressCoords[0], addressCoords[1], 49.2412, -123.0298);

    const params = new URLSearchParams({
      origin: origin,
      destination: destination,
      mode: 'transit',
      arrival_time: Math.floor(arriveTime.getTime() / 1000),
      key: apiKey,
    });
  
    const url = `${apiUrl}?${params.toString()}`;
    // const url = 'https://transit.router.hereapi.com/v8/routes?origin=49.1331,-123.0011&destination=49.2668,-123.2456&apiKey=S3186X1u-4DFckek542dcP9gxZeLI3uHQl_IkwZnJb4'
    const request = https.get(url, (response) => {
      let data = '';
      console.log("planTransitTrip(): request sent");
  
      response.on('data', (chunk) => {
        data += chunk;
      });
  
      response.on('end', () => {
        console.log("planTransitTrip() return");
        console.log("--------------------------------------------------------");
        var routes = JSON.parse(data).routes;
        if (routes.length > 0) {
          // Process each route
          routes.forEach((route, index) => {
            // Access route information such as summary, distance, duration, steps, etc.
            console.log(`Route ${index + 1}:`);
            console.log(`Summary: ${route.summary}`);
            console.log(`Distance: ${route.legs[0].distance.text}`);
            console.log(`Duration: ${route.legs[0].duration.text}`);
            console.log('Steps:');
            route.legs[0].steps.forEach((step, stepIndex) => {
              console.log(`Step ${stepIndex + 1}: ${step.html_instructions}`);
            });
            console.log('-----------------------');
          });
        } else {
          console.log('No routes found.');
        }
        console.log("--------------------------------------------------------");
        console.log(data);
        console.log(JSON.parse(data));
        console.log("--------------------------------------------------------");
        resolve(JSON.parse(data));
      });
    })
    request.on("error", (err) => {
        console.log("Error: " + err.message);
    });
  });
}

function calcDist(x1, y1, x2, y2) {
  var a = x1 - x2;
  var b = y1 - y2;

  return Math.sqrt( a*a + b*b );
}


connectToDatabase();
sslServer.listen(port, () => console.log('Secure server :) on port ' + port))
