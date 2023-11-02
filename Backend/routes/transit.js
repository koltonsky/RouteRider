

/**
 * TODO: display route map on frontend
 * 
 * time to arrive 
 * 
 * origin
 * destination
 */


var dummy_schedule = {
    email: 'koltonluu@gmail.com',
    events: [
      {
        address: 'UBC MacLeod, Room 4006',
        calendarID: 'koltonluu@gmail.com',
        endTime: '2023-11-01T15:00:00.000-07:00',
        eventName: 'CPEN 321 L1B',
        geolocation: { latitude: 0, longitude: 0 },
        id: '_64p36d1h6osjgchm6cp3gchk68r62oj3cgp3ge9h6krg_20231101T200000Z',
        startTime: '2023-11-01T13:00:00.000-07:00'
      },
      {
        address: 'UBC MacMillan, Room 360',
        calendarID: 'koltonluu@gmail.com',
        endTime: '2023-11-01T17:00:00.000-07:00',
        eventName: 'CPEN 321 101',
        geolocation: { latitude: 0, longitude: 0 },
        id: '_64p36d1h6osjgchm6cp3gchk68r62oj3cgpj4d1m64rg_20231101T223000Z',
        startTime: '2023-11-01T15:30:00.000-07:00'
      },
      {
        address: 'UBC Nest Building, Room 360',
        calendarID: 'koltonluu@gmail.com',
        endTime: '2023-11-03T17:00:00.000-07:00',
        eventName: 'Club Meeting',
        geolocation: { latitude: 0, longitude: 0 },
        id: '_64p36d1h6osjgchm6cp3gchk68r62oj3cgpj4d1m64tr_20231101T223000Z',
        startTime: '2023-11-03T15:30:00.000-07:00'
      }
    ]
  }
  var dummy_user = {
    email: "koltonluu@gmail.com",
    name: "John Doe",
    address: "5870 Rumble Street, Burnaby, BC",
    friends: [
      "friend1@example.com",
      "friend2@example.com"
    ]
  }
  
  // client.db('ScheduleDB').collection('schedulelist').insertOne(dummy_schedule).then((result) => {
  //   console.log("inserted schedule");
  // });
  // client.db('UserDB').collection('userlist').insertOne(dummy_user).then((result) => {
  //   console.log("inserted user");
  // });
  
  // var dummy = initRoute("koltonluu@gmail.com", "2023-11-01");
  var dummy = initReminders("koltonluu@gmail.com");
  /**
   * Function returns an array of objects containing the following fields:
   *    _id: bus number/Skytrain line name (e.g. 99, R4, Expo Line, etc.)
   *    _leaveTime: time to leave
   *    _leaveTimeNum: time to leave in number format
   *    _type: type of transportation (Bus, SkyTrain, Walk)
   * The array represents the transit route the user should take to arrive at their destination on time.
   * 
   * For local testing, connect to a mongoDB instance and run commented client.db commands above. They should initialize the database with dummy data.
   *  
   * Translink Open API Key: crj9j8Kj97pbPkkc61dX
   * Geocoding API key: AAPK3c726265cc41485bb57c5512e98cf912OLoJQtidjOlcqjdpa0Pl773UqNoOYfwApr6ORYd8Lina8_K0sEbdcyXsNfHFqLKE if error 498 invalid token, create a new key
   * HERE Location services API key (unused): S3186X1u-4DFckek542dcP9gxZeLI3uHQl_IkwZnJb4
   *                        App ID  (unused):  cOIE7nteY1IGtsu8BGpr
   * Google Direction API ($200 credit): AIzaSyBVsUyKxBvRjE0XdooMuoDrpAfu1KO_2mM 
   * 
   * @param {*} userEmail 
   * @param {*} date
   */
  const initRoute = async => (req, res) {
    console.log("called initRoute()");
    
    var schedule = await client.db('ScheduleDB').collection('schedulelist').findOne({email: req.body.userEmail});
    var user = await client.db('UserDB').collection('userlist').findOne({email: req.body.userEmail})
    console.log("initRoute(): returned schedule: " + schedule);
    console.log(schedule.events[0].eventName);
  
    /* Initialize fields that are need for Directions API call */
    var timeOfFirstEvent = "";
    var locationOfFirstEvent = "";
    var locationOfOrigin = user.address;
    for (var i = 0; i < schedule.events.length; i++) { // assumes events are sorted by date
      // console.log("initRoute(): " + schedule.events[i].startTime + " " + date);
      if (schedule.events[i].startTime.split('T')[0] == req.body.date) { 
        // console.log("initRoute(): startTime " + schedule.events[i].startTime);
        timeOfFirstEvent = schedule.events[i].startTime;
        locationOfFirstEvent = schedule.events[i].address;
        locationOfFirstEvent = locationOfFirstEvent.split(',')[0];
        break;
      }
    }
    console.log("initRoute(): returned timeOfFirstEvent: " + timeOfFirstEvent);
    console.log("initRoute(): returned locationOfFirstEvent: " + locationOfFirstEvent);
    console.log("initRoute(): returned locationOfOrigin: " + locationOfOrigin);
  
    planTransitTrip(locationOfOrigin, locationOfFirstEvent, new Date(timeOfFirstEvent)).then((trip) => {
      console.log("initRoute(): returned trip: " + trip + " " + trip.routes[0].legs[0].steps[0].travel_mode);
      /* fields for object to be returned to frontend */
      var id = '';
      var leaveTime = '';
      var type = '';
      var more = {};
      var curStep = {}; // maybe properly define this object later
      var returnList = [];
  
      more.distance = trip.routes[0].legs[0].distance.text;
      more.duration = trip.routes[0].legs[0].duration.text;
      more.arrival_time = trip.routes[0].legs[0].arrival_time.text;
      more.departure_time = trip.routes[0].legs[0].departure_time.text;
      more.steps = [];
    
      trip.routes[0].legs[0].steps.forEach((step, stepIndex) => {
        travelMode = step.travel_mode;
    
        if (travelMode == "TRANSIT") {
          switch(step.transit_details.line.vehicle.name) {
            case("Bus"):
              type = "Bus";
              id = step.transit_details.line.short_name;
              leaveTime = step.transit_details.departure_time.text;  
              leaveTimeNum = step.transit_details.departure_time.value;
              break;
            case("Subway"):
              type = "SkyTrain";
              id = step.transit_details.line.name;
              leaveTime = step.transit_details.departure_time.text; 
              leaveTimeNum = step.transit_details.departure_time.value;
              break;
            default:
              type = "default";
              id = "default";
              break;
          }
        }
        else {
          type = "Walk";
          id = "Walk";
          leaveTime = step.duration.text; 
          leaveTimeNum = step.duration.value;
        }
        
        more.steps.push(step.html_instructions);
        console.log("initRoute(): adding curStep to returnList " + id + " | " + leaveTime + " | " + type);
        curStep = {_id: id, _leaveTime: leaveTime, _leaveTimeNum: leaveTimeNum, _type: type};
        returnList.push(curStep);
      });
      returnList.push(more);
  
      /* Directions API doesn't include leaveTime in "WALKING" steps, so we need to calculate ourselves */
      for (var i = 0; i < returnList.length - 1; i++) {
        if (i == 0 && returnList[i]._type == "Walk") { 
          returnList[i]._leaveTime = returnList[returnList.length - 1].departure_time;
          returnList[i]._leaveTimeNum = timeToTimestamp(returnList[returnList.length - 1].departure_time);
        }
        else if ((i == returnList.length - 2) && returnList[i]._type == "Walk") { 
          returnList[i]._leaveTime = returnList[returnList.length - 1].arrival_time;
          returnList[i]._leaveTimeNum = timeToTimestamp(returnList[returnList.length - 2].arrival_time);
        }
        else if (returnList[i]._type == "Walk") { // assumes 'type' for next array entry is either "Bus" or "SkyTrain"
          returnList[i]._leaveTime = timestampToTime(returnList[i + 1]._leaveTimeNum - returnList[i]._leaveTimeNum);
          returnList[i]._leaveTimeNum = returnList[i + 1]._leaveTimeNum - returnList[i]._leaveTimeNum;
        }
        console.log("updated leavetime: " + returnList[i]._leaveTime);
      }
    
      res.status(200).json({"returnList": returnList});
    });
  }
  
  /**
   * Pretty much same logic as initRoute, but we only care about time to leave for the first step of each trip (when to leave the house)
   * The time to leave determined by initRoute and initReminders should be the same 
   * 
   * @param userEmail
   * @returns an array of objects that contain info on when to send notifications (time and date)
   */
  const initReminders = async (req, res) => {
    console.log("called initReminders()");
    var returnList = []
    
    var schedule = await client.db('ScheduleDB').collection('schedulelist').findOne({email: req.body.userEmail});
    var user = await client.db('UserDB').collection('userlist').findOne({email: req.body.userEmail})
    console.log("initReminders(): returned schedule: " + schedule);
    console.log(schedule.events[0].eventName);
  
    /* Get an array that contains the first event of each day */
    var firstEvents = [];
    var date = "default";
    for (var i = 0; i < schedule.events.length; i++) { // assumes events are sorted by date
      if (schedule.events[i].startTime.split('T')[0] != date) { 
        var event = {};
        event.locationOfOrigin = user.address;
        event.timeOfFirstEvent = schedule.events[i].startTime;
        event.locationOfFirstEvent = schedule.events[i].address.split(',')[0];
        
        firstEvents.push(event);
  
        date = schedule.events[i].startTime.split('T')[0];
      }
    }
    
    for (var i = 0; i < firstEvents.length; i++) {
      trip = await planTransitTrip(firstEvents[i].locationOfOrigin, firstEvents[i].locationOfFirstEvent, new Date(firstEvents[i].timeOfFirstEvent));
      console.log("initReminders(): returned trip: " + trip + " " + trip.routes[0].legs[0].steps[0].travel_mode);
      /* fields for object to be returned to frontend */
      var reminder = {
        date: 'default',
        leaveTime: 'default',
      };
  
      reminder.date = firstEvents[i].timeOfFirstEvent;
      reminder.leaveTime = departure_time = trip.routes[0].legs[0].departure_time.text;
  
      returnList.push(reminder);
    }
    returnList.push({email: userEmail});
    console.log("initReminders(): returned returnList: " + returnList.length); 
    // for (var i = 0; i < returnList.length; i++) {
    //   console.log("initReminders(): returned returnList: " + returnList[i].date + " " + returnList[i].leaveTime); 
    // }
  
    res.status(200).json({"returnList": returnList});
  }
  // getNearestBuses("EB Rumble St @ Gilley Ave").then((buses) => {
  //   console.log(buses[0].StopNo + " " + buses.length);
  // });
  app.route('/api/reminderlist/:email').get((req, res) => {
    var test = initReminders(req.params.email);
  });
  
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
        console.log("getNearestBuses(): returned coords: " + coords);
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
    
      // determine whether to take 99 B-Line or R4 based on address. unused for now
      // var addressCoords = getLatLong(origin);
      // var distToCommercial = calcDist(addressCoords[0], addressCoords[1], 49.2624, -123.0698);
      // var distToJoyce = calcDist(addressCoords[0], addressCoords[1], 49.2412, -123.0298);
  
      const params = new URLSearchParams({
        origin: origin,
        destination: destination,
        mode: 'transit',
        arrival_time: Math.floor(arriveTime.getTime() / 1000),
        key: apiKey,
      });
    
      const url = `${apiUrl}?${params.toString()}`;
  
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
          // console.log(data);
          // console.log(JSON.parse(data));
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
  
  function timeToTimestamp(timeString) {
    const date = new Date(timeString);
    return Math.floor(date.getTime() / 1000);
  }
  
  function timestampToTime(timestamp) {
    const date = new Date(timestamp * 1000);
    return date.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' });
  }
  
  function combineDateAndTime(dateString, timeString) {
    // Convert the time to 24-hour format and add seconds
    const timeComponents = timeString.match(/(\d+):(\d+) (A|P)M/);
    if (!timeComponents) {
        throw new Error("Invalid time format");
    }
    
    const hours = parseInt(timeComponents[1]);
    const minutes = parseInt(timeComponents[2]);
    const isPM = timeComponents[3].toLowerCase() === 'pm';
  
    // Adjust hours for PM
    if (isPM && hours < 12) {
        hours += 12;
    }
  
    const isoDateTimeString = `${dateString}T${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:00`;
    const date = new Date(isoDateTimeString);
  
    if (isNaN(date)) {
        throw new Error("Invalid date or time format");
    }
  
    return date.toISOString();
  }