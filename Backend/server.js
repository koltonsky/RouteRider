const express = require('express');
const https = require('https');
const path = require('path');
const fs = require('fs');
// const queryString = require('querystring');
const port = 8081;
// const fetch = require('node-fetch');
const { MongoClient } = require('mongodb');
// const ApiKeyManager = require('@esri/arcgis-rest-request');
const { ApiKeyManager } = require('@esri/arcgis-rest-request');
const { geocode } = require('@esri/arcgis-rest-geocoding');
const util = require('util'); // Add this line

const readFile = util.promisify(fs.readFile);
const serverListen = util.promisify(https.createServer().listen);

const app = express();
app.use(express.json());

const user = require('./routes/user.js');
const schedule = require('./routes/schedule.js');
const commuters = require('./commuter_match.js');
const recommendation = require('./recommendation.js');
// const { get } = require('http');
// const { time, error } = require('console');

const admin = require('firebase-admin');

// Retrieve the private key from the environment variable
//const privateKey = process.env.PRIVATE_KEY;
const serviceAccountType = process.env.SERVICE_ACCOUNT_TYPE;

const privateKeyPart1 =
  '-----BEGIN PRIVATE KEY-----\nMIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDWxaIVXYR7Tb4Z';
const privateKeyPart2 =
  'ASAdm0+qupaOq1rRNReRDLSdOgexVMgsef2wGis74oI/Vtneg/iIqxPrAVbKwHTp';
const privateKeyPart3 =
  'WSMntGoZUV2aRPOiAgBlHu5cXN0Ql0++r5nhAImRgsKxnsbIJZaX9L1huU8xzAlA';
const privateKeyPart4 =
  '2LZCoHkSacLmFsVzfTzU0mOKBBc/AzqH99ucTNiQljEsaatRUdnGg6z7B91O7d/V';
const privateKeyPart5 =
  '3K6E/RefXicJ1lfntWVYxc5tmkPArM56Sqq/DesaLlIEodt8HRBbXemmXz9PXpQz';
const privateKeyPart6 =
  '4fmth95XzD1mBXwI+YZ8ffaedMN6FOjkSvWrhvtdHeO13vwlBA1GC7xDykCEuoY7';
const privateKeyPart7 =
  'V7vfWoOFAgMBAAECggEASQfJFrTHENqdsoj0b7zZOTfbbEYOSqdgDR2h6PjLltw6';
const privateKeyPart8 =
  'eQ0+W3x6iRF7sqgIy6Zag7aQvk+lQKpy1spNrvmlPlixmHyrz8IYekorSVL2hOa+';
const privateKeyPart9 =
  '4ht6Gs2A+e7Z32YbOAG4FJHPOAS4TjmQR/GpADzrDnzSHkVN/PhwD/o+iLbdZLpH';
const privateKeyPart10 =
  'trprP0vcQSIY45SkDXj7Z7eBU3pH6I9r4uCwIAP8WUmP2wKswFLtC/ceDm6Va/pZ';
const privateKeyPart11 =
  'dDZWKTxQiz5RpM6kuIRwMkjCQorFzPaLAoZtjCtbysnipPXAB9zNtL3jrT5TVrMA';
const privateKeyPart12 =
  '/bqdEFSuPvs6OaclfPn3Ih56wGJdCSLjwxcZpdAVPQKBgQD0weCGaWtIWg/E/142';
const privateKeyPart13 =
  'mj9OpWQmgNS1v9uWudkDrP2/S86urvVJt0v3lrFNeOTFzMsdoxFFNzkRGg+db33n';
const privateKeyPart14 =
  'pg1RJKU81oBMh+s6Fnxg7Q1Wt+/7QBqjiznKVNU1fCo5CM0223viojE+PJfl2Spn';
const privateKeyPart15 =
  'CFS1woDUZ/WYm+SIAKf+UWd7NwKBgQDgoyfuvp1JWKxsUUgpC5vg5hzWM3yrnx2h';
const privateKeyPart16 =
  'UaGwpfq5DGVmrjoHevswolaOj/SvymoIEhms2abbowPTaejU3Sizf+i4oRTspdWG';
const privateKeyPart17 =
  'YCVedQp9/wblK3A3WSytJVrXUJvjkAc+DDa3p1Zr4ScUQ6QbkofM6mUi2U1W2P7V';
const privateKeyPart18 =
  'HZQgnNQtIwKBgQCB23dXeQj9iyMAvwhqae4auO9o6kNw5okH8DSumZLLctoGnjbv';
const privateKeyPart19 =
  '1HtOsjoBw5mFRIGjiMf59DGn3C7atbOUOuqn2Yx9ucS6Vga8e/+joUHJd6+wmzNG';
const privateKeyPart20 =
  '//A6ZEX2qZjxR7UxXMPe23TK83UX8t9naOkgwkB98WZBgLyAV/DJosEHgwKBgQDN';
const privateKeyPart21 =
  'zy3q4uEgLgnrQ50lXel2591LsuhqJOH0xuGpAqjvmZfdt4qbB+XT7Sf4fZPk60Ky';
const privateKeyPart22 =
  'GkNDxjXFzVjX/ZTAUc/UhUAmyA5vspArCTOzkvAF9/3NQTsSurTf/fV4h/YLTA4W';
const privateKeyPart23 =
  'nwISyVG4jRRM0JwuVtXsvGPkxcrB4xW3E95+8rDCmQKBgARMkmxwl3Q9InjAPONr';
const privateKeyPart24 =
  'CtLaYdjeZdRtKQOic4092lRdtAIrZvZ7SHlaFUp8LULFY6BzxzdjydMa2BiPb9mA';
const privateKeyPart25 =
  'mXHQVdvGv5x30soQ3EtQocPkj7xyY4glrG7hSKYHPtFpHlkakQWBrvCjeYJb0g+E';
const privateKeyPart26 =
  '+EsDxo7zRKeT+9mNDQYTSX7S\n-----END PRIVATE KEY-----\n';

const privateKey = `${privateKeyPart1}\n${privateKeyPart2}\n${privateKeyPart3}\n${privateKeyPart4}\n${privateKeyPart5}\n${privateKeyPart6}\n${privateKeyPart7}\n${privateKeyPart8}\n${privateKeyPart9}\n${privateKeyPart10}\n${privateKeyPart11}\n${privateKeyPart12}\n${privateKeyPart13}\n${privateKeyPart14}\n${privateKeyPart15}\n${privateKeyPart16}\n${privateKeyPart17}\n${privateKeyPart18}\n${privateKeyPart19}\n${privateKeyPart20}\n${privateKeyPart21}\n${privateKeyPart22}\n${privateKeyPart23}\n${privateKeyPart24}\n${privateKeyPart25}\n${privateKeyPart26}`;

// Create the serviceAccount object

const serviceAccount = {
  type: serviceAccountType,
  project_id: 'routerider-402800',
  private_key_id: '5bcd35ff287cd344df63e9bd5d96170fdc72130a',
  private_key: privateKey,
  client_email:
    'firebase-adminsdk-stvy2@routerider-402800.iam.gserviceaccount.com',
  client_id: '107472218462534326183',
  auth_uri: 'https://accounts.google.com/o/oauth2/auth',
  token_uri: 'https://oauth2.googleapis.com/token',
  auth_provider_x509_cert_url: 'https://www.googleapis.com/oauth2/v1/certs',
  client_x509_cert_url:
    'https://www.googleapis.com/robot/v1/metadata/x509/firebase-adminsdk-stvy2%40routerider-402800.iam.gserviceaccount.com',
  universe_domain: 'googleapis.com',
};


//console.log(privateKey);
// Use the serviceAccount object in your code

//const serviceAccount = require('./serviceAccountKey.json');

const cron = require('node-cron');
// const { get } = require('http');
// const { error } = require('console');
// const e = require('express');

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
  databaseURL: 'https://routerider-402800.firebaseio.com',
});

// MongoDB connection setup
const uri = 'mongodb://127.0.0.1:27017'; // Replace with your MongoDB connection string
const client = new MongoClient(uri);

/*
async function connectToDatabase() {
  try {
    await new Promise((resolve) => setTimeout(resolve, port));
    await client.connect();
    console.log('Connected to MongoDB');
  } catch (error) {
    console.error('MongoDB Connection Error:', error);
  }
}
*/

// =========== REST API CALL ENDPOINTS ==============

// User DB
app.post('/api/userlist', user.createNewUser);

app.get('/api/userlist/:email', user.getUserByEmail);
//app.get('/api/userlist/:email/name', user.getUserName);
//app.get('/api/userlist/:email/address', user.getUserAddress);
app.get('/api/userlist/:email/friends', user.getFriendListWithNames);

app.put('/api/userlist/:email/address', user.updateAddress);

//app.post('/api/userlist/:email/friends', user.addFriend);
//app.delete('/api/userlist/:email/friends', user.deleteFriend);

app.post('/api/userlist/:email/friendRequest', user.sendFriendRequest);

app.post(
  '/api/userlist/:email/:friendRequest/accept',
  user.acceptFriendRequest
);
app.delete(
  '/api/userlist/:email/:friendRequest/decline',
  user.declineFriendRequest
);

//app.put('/api/userlist/:email', user.updateUser);

// test/db purposes only
//app.delete('/api/userlist/:email', user.deleteUser);

// Schedule DB
app.post('/api/schedulelist', schedule.createNewSchedule);
app.get('/api/schedulelist/:email', schedule.getScheduleByEmail);

app.put('/api/schedulelist/:email', schedule.updateSchedule);

app.get('/api/schedulelist/:email/:id', schedule.getCalendarID);

app.post('/api/schedulelist/:email', schedule.addEvent);
//app.put('/api/schedulelist/:email/:id', schedule.editEventByID);
app.delete('/api/schedulelist/:email/:id', schedule.deleteEventByID);

//app.put('/api/schedulelist/:email/:index/geolocation', schedule.editEventGeolocation);

//app.delete('/api/schedulelist/:email/', schedule.deleteSchedule);

/**
 * ChatGPT usage: Partial
 */
app.post('/api/store_token', (req, res) => {
  const token = req.body.token;
  const email = req.body.email;
  if (!token || !email) {
    // console.log('Invalid email or token');
    // console.log('token: ' + token);
    res.status(400).json({ message: 'Invalid email or token' });
    return;
  }

  // Attempt to find the user
  client
    .db('UserDB')
    .collection('userlist')
    .findOne({ email })
    .then((user) => {
      if (user) {
        // Update the 'fcmToken' field in the database for the found entry
        client
          .db('UserDB')
          .collection('userlist')
          .updateOne({ _id: user._id }, { $set: { fcmToken: token } })
          .then(() => {
            // console.log('fcmToken updated successfully');
            res.status(200).json({
              message: 'fcmToken updated successfully',
            });
          });
      } else {
        // console.log('User not found');
        res.status(400).json({
          message: 'User not found, failed to update fcmToken',
        });
      }
    });
  // .catch((error) => {
  //   // console.log('Error: ' + error);
  //   res.status(401).json({ message: error });
  // });
});

/**
 * ChatGPT usage: Partial
 */
app.post('/api/send-friend-notification', async (req, res) => {
  const senderName = req.body.senderName;
  const receiverEmail = req.body.receiverEmail;

  // console.log("friend notif email: " + receiverEmail);
  // ret = await findUserToken(receiverEmail, senderName, sendNotification);
  // res.status(ret.status).json({ message: ret.message });
  findUserToken(receiverEmail, senderName, sendNotification).then((ret) => {
    res.status(ret.status).json({ message: ret.message });
  });
});
function findUserToken(receiverEmail, senderName, notificationCallback) {
  return new Promise((resolve, reject) => {
    if (!senderName || !receiverEmail) {
      // console.log('null email or name');
      resolve({ status: 400, message: 'Null receiver email or sender name' });
    }

    client
      .db('UserDB')
      .collection('userlist')
      .findOne({ email: receiverEmail })
      .then(async (receiver) => {
        if (receiver) {
          // console.log(
          //   'grabbed user: ' +
          //     receiver +
          //     ' ' +
          //     receiver.fcmToken +
          //     ' ' +
          //     receiver.email
          // );
          var receiverToken = receiver.fcmToken;

          // console.log(receiverToken);

          const message = {
            token: receiverToken,
            notification: {
              title: 'New Friend Request',
              body: `${senderName} has sent you a friend request!`,
            },
          };
          // console.log(message.token);

          var ret = await notificationCallback(message);
          if (ret) {
            // console.log('Successfully sent friend request notification');
            // res.status(200).json({
            //   message:
            //     'Successfully sent friend request notification',
            // });
            // return { status: 200, message: 'Successfully sent friend request notification'}
            resolve({
              status: 200,
              message: 'Successfully sent friend request notification',
            });
          } else {
            // console.log('Failed to send friend request notification');
            // res.status(400).json({
            //   message:
            //     'Failed to send friend request notification',
            // });
            // return { status: 400, message: 'Failed to send friend request notification'}
            resolve({
              status: 400,
              message: 'Failed to send friend request notification',
            });
          }
        } else {
          // console.log('Receiver not found');
          // res.status(400).json({
          //   message:
          //     'Receiver not found, failed to send notification',
          // });
          // return { status: 400, message: 'Receiver not found, failed to send notification'}
          resolve({
            status: 400,
            message: 'Receiver not found, failed to send notification',
          });
        }
      });
  });
}
function sendNotification(message) {
  admin
    .messaging()
    .send(message)
    .then((response) => {
       console.log('Successfully sent message:' + response);
      // res.status(200).json({message: 'Successfully sent friend request notification'});
      return 1;
    })
    .catch((error) => {
       console.error('Error sending message:', error);
      // res.status(400).json(error);
      return 0;
    });
}

/**
 * ChatGPT usage: None
 */
app.post('/api/initReminders', async (req, res) => {
  // console.log('*****called initReminders api endpoint*****');
  // initReminders(req, sendNotification)
  //   .then(() => {
  //     res.status(200).json({ message: 'Reminders initialized' });
  //   })
  //   .catch((error) => {
  //     console.log('initReminders api endpoint connection error');
  //     res.status(500).json({ message: error });
  //   });
  let ret = await initReminders(req, sendNotification);
  res.status(ret.status).json({ message: ret.message });
});

// Find commute buddy
app.get('/api/findMatchingUsers/:userEmail', async (req, res) => {
  const userEmail = req.params.userEmail;

  await commuters.findMatchingUsers(userEmail).then((result) => {
    console.log("matching users result: ");
    console.log(result);
      return res.status(200).json({ matchingUsers: [...result] });
    },
    (error) => {
      // console.log('initRouteWithFriends rejected error');
      return res.status(400).json({ message: error });
    });
});

// app.get('/api/initRouteWithFriends', async (req, res) => {
//   const userEmail = req.params.userEmail;
//   const friendEmail = req.params.friendEmail;
//   const date = req.params.date;

//   initRouteWithFriends(userEmail, friendEmail, date).then((sharedRoute) => {
//     res.json({ sharedRoute });
//   }).catch(error => {
//     console.log("initRouteWithFriends api endpoint connection error");
//     res.status(500).json({ message: error });
//   });
// });

/**
 * ChatGPT usage: None
 */
const getRecommendedRoutesWithFriends = async (req, res) => {
  const email = req.params.email;
  const friendEmail = req.params.friendEmail;
  const date = req.params.date;
  await initRouteWithFriends(email, friendEmail, date).then(
    (result) => {
      return res.status(200).json({ routes: result });
    },
    (error) => {
      // console.log('initRouteWithFriends rejected error');
      return res.status(400).json({ message: error });
    }
  );
};

app.get(
  '/api/recommendation/routesWithFriends/:email/:friendEmail/:date',
  getRecommendedRoutesWithFriends
);

// RecommendationFinder

const getRecommendedRoutes = async (req, res) => {
  const email = req.params.email;
  const date = req.params.date;
  // const result = await initRoute(email, date);
  // return res.status(200).json({ routes: result });

  await initRoute(email, date).then(
    (result) => {
      return res.status(200).json({ routes: result });
    },
    (error) => {
      // console.log('initRoute rejected error ' + error);
      return res.status(400).json({ message: error });
    }
  );
};
const getWeeklyRecommendedRoutes = async (req, res) => {
  console.log('getting weekly routes');
  const email = req.params.email;
  const date1 = new Date(req.params.date1);
  const date2 = new Date(req.params.date2);
  console.log('date1 parsed:' + date1);
  console.log('date2 parsed:' + date2);

  const result = [];
  for (var d = date1; d <= date2; d.setDate(d.getDate() + 1)) {
    try {
      console.log('d:' + d);
      var date = d.toISOString().split('T')[0];
      console.log('d parsed:' + date);
      const routes = await initRoute(email, date);
      console.log({ date, routes });
      result.push({ date, routes });
    } catch (error) {
      console.error('Error in getWeeklyRecommendedRoutes:', error);
    }
  }
  if (result.length !== 0) {
    res.status(200).json({ weeklyRoutes: result });
  } else {
    res
      .status(400)
      .json({ message: 'An error occurred while processing the request.' });
  }
};

app.get('/api/recommendation/routes/:email/:date', getRecommendedRoutes);
app.get(
  '/api/recommendation/routes/:email/:date1/:date2',
  getWeeklyRecommendedRoutes
);

const getTimeGapRecommendations = async (req, res) => {
  // try {
  //   const addr1 = req.params.addr1;
  //   const addr2 = req.params.addr2;
  //   const result = await recommendation(addr1, addr2);
  //   return res.status(200).json({ suggestions: result });
  // } catch (error) {
  //   console.error('Error in /api/recommendation/timegap/:addr1/:addr2', error);
  //   return res.status(500).json({ error: 'An error occurred' });
  // }
  const addr1 = req.params.addr1;
  const addr2 = req.params.addr2;
  recommendation(addr1, addr2).then((result) => {
    return res.status(200).json({ suggestions: result });
  }, (error) => {
    console.error('Error in /api/recommendation/timegap/:addr1/:addr2', error);
    return res.status(500).json({ error: 'An error occurred' });
  });
};

app.get('/api/recommendation/timegap/:addr1/:addr2', getTimeGapRecommendations);

// could split this into two functions, one for checking and one for sending notifs
// checkLiveTransitTime("xxx", '123', '51439', "xxx");
/**
 * Compares a specific bus's expected leave time with the scheduled leave time and sends a
 * notification to the user if the bus is off schedule.
 *
 * @param {*} userEmail
 * @param {*} busNumber
 * @param {*} stopNumber
 * @param {*} scheduledLeaveTime
 * @returns true/false, indicating whether a notification was sent
 *
 * ChatGPT usage: Partial
 */
async function checkLiveTransitTime(
  userEmail,
  busNumber,
  stopNumber,
  scheduledLeaveTime,
  notificationCallback
) {
  return new Promise((resolve, reject) => {
    // console.log('its time!');
    const apiKey = 'crj9j8Kj97pbPkkc61dX';
    client
      .db('UserDB')
      .collection('userlist')
      .findOne({ email: userEmail })
      .then((user) => {
        var userToken = user.fcmToken;

        //var userToken = "dJ8XXSK2T32bxcPjCULZWq:APA91bGk55eHZuyVN59hWw0313o_63OMptOnOqgHIEmBT5g9jIaYouLDGvtT-Knybc6UTHn2L4G7qh7osMSstIpIAd4Nt3Uy_k9_SHJkMoSgoPiZ2c2QJXhvXZDnkMlYQuiyZB2gizK4"

        const apiUrl = `https://api.translink.ca/rttiapi/v1/stops/${stopNumber}/estimates?apikey=${apiKey}&count=3&timeframe=120&routeNo=${busNumber}`;

        const options = {
          method: 'GET',
          headers: {
            Accept: 'application/JSON',
          },
        };

        const req = https.request(apiUrl, options, (res) => {
          let data = '';

          res.on('data', (chunk) => {
            data += chunk;
          });

          res.on('end', () => {
            if (res.statusCode === 200) {
              const realTimeData = JSON.parse(data);
              // console.log(realTimeData);
              // console.log(realTimeData[0].Schedules);
              // console.log(realTimeData[0].Schedules[0].ExpectedLeaveTime);
              // console.log(realTimeData[0].Schedules[0].ExpectedCountdown);

              if (
                !compareTimeStrings(
                  realTimeData[0].Schedules[0].ExpectedLeaveTime,
                  scheduledLeaveTime
                )
              ) {
                // console.log('bus is off schedule');

                const message = {
                  token: userToken,
                  notification: {
                    title: `The ${busNumber} bus at stop ${stopNumber} is off schedule!`,
                    body: `The expected vs. actual ETA is ${realTimeData[0].Schedules[0].ExpectedLeaveTime} vs ${scheduledLeaveTime}`,
                  },
                };

                let ret = notificationCallback(message);
                if (ret) {
                  // console.log('Successfully sent notification');
                  resolve(true);
                } else {
                  // console.log('Failed to send notification');
                  resolve(false);
                }
              } else {
                resolve(false);
              }
            } else {
              console.error(
                `API request failed with status: ${res.statusCode}`
              );
              resolve(false);
            }
          });
        });

        req.end();
      });
  });
}

// function test() {
//   console.log("test");
//   const scheduledReminders = {
//     email: "koltonluu@gmail.com",
//     date: "123",
//     reminders: []
//   }
//   client.db("ReminderDB").collection("reminderlist").findOne({email: "koltonluu@gmail.com", date: "123"}).then((existingReminders) => {
//     if (existingReminders != null) {
//       console.log("removing reminders for " + existingReminders.date);
//       existingReminders.reminders.forEach((existingReminder) => {
//         existingReminder.stop(); // DEBUG: this line is prob the issue, might need to use chatgpt implementation involving checking for an 'active' field
//       });
//     }
//   });
//   for (var i = 1; i < 6; i++) {
//     var currentDate = new Date();
//     var futureDate = new Date(currentDate.getTime() + i*5000);
//     var isoTimestamp = futureDate.toISOString();
//     cronTime = isoToCron(isoTimestamp, 0);
//     console.log("time: " + cronTime + " " + isoTimestamp);
//     scheduledReminders.reminders[i - 1] = cron.schedule(cronTime, () => {
//       checkLiveTransitTime("koltonluu@gmail.com", '123', '51408', "9:20pm").then((ret) => {
//         if(ret) {
//           scheduledReminders.reminders.forEach(element => {
//             element.stop();
//           });
//         }
//       });
//     });
//   }
//   client.db("ReminderDB").collection("reminderlist").insertOne(scheduledReminders).then((result) => {
//     console.log("planned reminders for " + scheduledReminders.date);
//   });
// }
//test();
/*
app.get('/api/userlist', async (req, res) => {
    const collection = client.db('UserDB').collection('userlist');
    const documents = await collection.find({}).toArray();
    res.json(documents);
  });
  
  // Get a single document by ID
app.get('/api/userlist/:id', async (req, res) => {
    const collection = client.db('UserDB').collection('userlist');
    const document = await collection.findOne({ _id: req.params.id });
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
    const result = await collection.updateOne({ _id: req.params.id }, updatedDocument);
    res.json(result);
  });

// Get a person's schedule by ID
app.get('/api/schedulelist/:id', async (req, res) => {
  const collection = client.db('ScheduleDB').collection('schedulelist');
  const schedule = await collection.findOne({ _id: req.params.id });
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
*/

/*
app.use('/', (req, res, next) => {
  res.send('Hello from SSL server');
});
*/

/*
const sslServer = https.createServer(
  {
    key: fs.readFileSync(path.join(__dirname, 'certification', 'test_key.key')),
    cert: fs.readFileSync(
      path.join(__dirname, 'certification', 'certificate.pem')
    ),
  },
  app
);
*/

let sslServer;

async function startSSLServer() {
  const [key, cert] = await Promise.all([
    readFile(path.join(__dirname, 'certification', 'test_key.key')),
    readFile(path.join(__dirname, 'certification', 'certificate.pem')),
  ]);

  sslServer = https.createServer({ key, cert }, app);

  await serverListen.call(sslServer, port);

  console.log('Secure server :) on port ' + port);
  //  console.error('Error starting the server:', error);
  //  throw error; // Rethrow the error to propagate it to the caller
  //}
}

async function stopSSLServer() {
  return new Promise((resolve) => {
    if (sslServer) {
      sslServer.close(() => {
        console.log('Secure server stopped.');
        resolve();
      });
    }
  });
}

function connectToDatabase() {
  return new Promise((resolve, reject) => {
    console.log('Connected to MongoDB');

    // Start the SSL server after successfully connecting to MongoDB
    startSSLServer();

    resolve(); // Resolve the promise if startSSLServer completes successfully
  });
}

//connectToDatabase();
// Usage:

/*
connectToDatabase()

  .then(() => {
    // Do something after the server has started

    // Call stopSSLServer when you want to stop the server
    return stopSSLServer();
  }//)
  //.catch((error) => {
  //  console.error('Error starting or stopping server:', error);
  //}
  );
  */

//stopSSLServer();

/*
function closeServer() {
  if (sslServer) {
    console.log("CLOSING SERVER");

    sslServer.close((err) => {
      if (err) {
        console.error('Error closing server:', err);
      } else {
        console.log('Server closed');
      }
    });
  }
}
*/

//connectToDatabase();
// sslServer.listen(port, () => console.log('Secure server :) on port ' + port));

// var dummy_schedule = {
//   email: 'koltonluu@gmail.com',
//   events: [
//     {
//       address: 'UBC MacLeod, Room 4006',
//       calendarID: 'koltonluu@gmail.com',
//       endTime: '2023-11-01T15:00:00.000-07:00',
//       eventName: 'CPEN 321 L1B',
//       geolocation: { latitude: 0, longitude: 0 },
//       id: '_64p36d1h6osjgchm6cp3gchk68r62oj3cgp3ge9h6krg_20231101T200000Z',
//       startTime: '2023-11-01T13:00:00.000-07:00',
//     },
//     {
//       address: 'UBC MacMillan, Room 360',
//       calendarID: 'koltonluu@gmail.com',
//       endTime: '2023-11-01T17:00:00.000-07:00',
//       eventName: 'CPEN 321 101',
//       geolocation: { latitude: 0, longitude: 0 },
//       id: '_64p36d1h6osjgchm6cp3gchk68r62oj3cgpj4d1m64rg_20231101T223000Z',
//       startTime: '2023-11-01T15:30:00.000-07:00',
//     },
//     {
//       address: 'UBC Nest Building, Room 360',
//       calendarID: 'koltonluu@gmail.com',
//       endTime: '2023-11-03T17:00:00.000-07:00',
//       eventName: 'Club Meeting',
//       geolocation: { latitude: 0, longitude: 0 },
//       id: '_64p36d1h6osjgchm6cp3gchk68r62oj3cgpj4d1m64tr_20231101T223000Z',
//       startTime: '2023-11-03T15:30:00.000-07:00',
//     },
//   ],
// };
// var dummy_user = {
//   email: 'koltonluu@gmail.com',
//   name: 'John Doe',
//   address: '5870 Rumble Street, Burnaby, BC',
//   friends: ['friend1@example.com', 'friend2@example.com'],
// };

// var dummy_schedule2 = {
//   email: 'leonguo@gmail.com',
//   events: [
//     {
//       address: 'UBC MacLeod, Room 4006',
//       calendarID: 'leonguo@gmail.com',
//       endTime: '2023-11-01T15:00:00.000-07:00',
//       eventName: 'CPEN 321 L1B',
//       geolocation: { latitude: 0, longitude: 0 },
//       id: '_64p36d1h6osjgchm6cp3gchk68r62oj3cgp3ge9h6krg_20231101T200000Z',
//       startTime: '2023-11-01T13:00:00.000-07:00',
//     },
//     {
//       address: 'UBC MacMillan, Room 360',
//       calendarID: 'leonguo@gmail.com',
//       endTime: '2023-11-01T17:00:00.000-07:00',
//       eventName: 'CPEN 321 101',
//       geolocation: { latitude: 0, longitude: 0 },
//       id: '_64p36d1h6osjgchm6cp3gchk68r62oj3cgpj4d1m64rg_20231101T223000Z',
//       startTime: '2023-11-01T15:30:00.000-07:00',
//     },
//     {
//       address: 'UBC Nest Building, Room 360',
//       calendarID: 'leonguo@gmail.com',
//       endTime: '2023-11-03T17:00:00.000-07:00',
//       eventName: 'Club Meeting',
//       geolocation: { latitude: 0, longitude: 0 },
//       id: '_64p36d1h6osjgchm6cp3gchk68r62oj3cgpj4d1m64tr_20231101T223000Z',
//       startTime: '2023-11-03T14:30:00.000-07:00',
//     },
//   ],
// };
// var dummy_user2 = {
//   email: 'leonguo@gmail.com', // fake email, wont work with google authentication
//   name: 'Leon Guo',
//   address: '7746 Berkley Street, Burnaby, BC',
//   friends: ['friend1@example.com', 'friend2@example.com'],
// };

// client
//   .db('ScheduleDB')
//   .collection('schedulelist')
//   .insertOne(dummy_schedule)
//   .then((result) => {
//     console.log('inserted schedule');
//   });
// client
//   .db('UserDB')
//   .collection('userlist')
//   .insertOne(dummy_user)
//   .then((result) => {
//     console.log('inserted user');
//   });

// CODE TO FIND COMMUTE BUDDIES
/*
commuters.findMatchingUsers("koltonluu@gmail.com").then(result => {
  console.log('Schedule for other users:');
  console.log(result);
});
*/

// client.db('ScheduleDB').collection('schedulelist').insertOne(dummy_schedule).then((result) => {
//   console.log("inserted schedule");
// });
// client.db('UserDB').collection('userlist').insertOne(dummy_user).then((result) => {
//   console.log("inserted user");
// });

// var dummy = initRoute("koltonluu@gmail.com", "2023-11-01");
// var dummy = initReminders('koltonluu@gmail.com');
/**
 * Function returns an array of objects containing the following fields:
 *    _id: bus number/Skytrain line name (e.g. 99, R4, Expo Line, etc.)
 *    _leaveTime: time to leave
 *    _leaveTimeNum: time to leave in number format
 *    _type: type of transportation (Bus, SkyTrain, Walk)
 * The array represents the transit route the user should take to arrive at their destination on time.
 *
 * The final object in the returned array contains the following fields:
 *    distance: distance of the trip
 *    duration: duration of the trip
 *    arrival_time: arrival time at destination
 *    departure_time: departure time from origin
 *    steps: an array of strings containing the directions for the trip
 *
 * For local testing, connect to a mongoDB instance and run commented client.db commands above. They should initialize the database with dummy data.
 *
 * Translink Open API Key: crj9j8Kj97pbPkkc61dX
 * Geocoding API key: AAPK3c726265cc41485bb57c5512e98cf912OLoJQtidjOlcqjdpa0Pl773UqNoOYfwApr6ORYd8Lina8_K0sEbdcyXsNfHFqLKE
 * Google Direction API ($200 credit): AIzaSyBVsUyKxBvRjE0XdooMuoDrpAfu1KO_2mM
 *
 * @param {*} userEmail the email of the client that's being served
 * @param {*} date      the date of the commute
 *
 * ChatGPT usage: Partial
 */
async function initRoute(userEmail, date) {
  // console.log('called initRoute()');
  var errorString = '';

  var schedule = await client
    .db('ScheduleDB')
    .collection('schedulelist')
    .findOne({ email: userEmail });
  var user = await client
    .db('UserDB')
    .collection('userlist')
    .findOne({ email: userEmail });
  if (user == null) {
    errorString = 'No matching email exists in user database';
  } else if (schedule == null) {
    errorString = 'No matching schedule exists in schedule database';
  }

  // console.log('initRoute(): returned schedule: ' + schedule);
  // console.log(schedule.events[0].eventName);

  /* Initialize fields that are need for Directions API call */
  if (schedule != null) {
    var timeOfFirstEvent = '';
    var locationOfFirstEvent = '';
    var locationOfOrigin = user.address;
    for (var i = 0; i < schedule.events.length; i++) {
      // assumes events are sorted by date
      // console.log("initRoute(): " + schedule.events[i].startTime + " " + date);
      if (schedule.events[i].startTime.split('T')[0] == date) {
        // console.log("initRoute(): startTime " + schedule.events[i].startTime);
        timeOfFirstEvent = schedule.events[i].startTime;
        locationOfFirstEvent = schedule.events[i].address;
        locationOfFirstEvent = locationOfFirstEvent.split(',')[0];
        break;
      }
    }
    if (timeOfFirstEvent == '' && errorString == '') {
      errorString = date + ': No matching date exists in user schedule';
    }
    // console.log('initRoute(): returned timeOfFirstEvent: ' + timeOfFirstEvent);
    // console.log('initRoute(): returned locationOfFirstEvent: ' + locationOfFirstEvent);
    // console.log('initRoute(): returned locationOfOrigin: ' + locationOfOrigin);
  }

  return new Promise((resolve, reject) => {
    if (errorString != '') {
      // console.log("error in initRoute(): " + errorString)
      reject(errorString);
    }

    planTransitTrip(
      locationOfOrigin,
      locationOfFirstEvent,
      new Date(timeOfFirstEvent)
    )
      .then((trip) => {
        if (trip.routes.length === 0) return;
        // console.log(
        //   'initRoute(): returned trip: ' +
        //     trip +
        //     ' ' +
        //     trip.routes[0].legs[0].steps[0].travel_mode
        // );

        /* fields for object to be returned to frontend */
        var id = '';
        var leaveTime = '';
        var leaveTimeNum = '';
        var type = '';
        var more = {};
        var curStep = {}; // maybe properly define this object later
        var returnList = [];

        more.distance = trip.routes[0].legs[0].distance.text;
        more.duration = trip.routes[0].legs[0].duration.text;
        more.arrival_time = trip.routes[0].legs[0].arrival_time.text;
        more.departure_time = trip.routes[0].legs[0].departure_time.text;
        more.steps = [];

        var travelMode = '';
        trip.routes[0].legs[0].steps.forEach((step, stepIndex) => {
          travelMode = step.travel_mode;

          if (travelMode == 'TRANSIT') {
            switch (step.transit_details.line.vehicle.name) {
              case 'Bus':
                type = 'Bus';
                id = step.transit_details.line.short_name;
                leaveTime = step.transit_details.departure_time.text;
                leaveTimeNum = step.transit_details.departure_time.value;
                break;
              case 'Subway':
                type = 'SkyTrain';
                id = step.transit_details.line.name;
                leaveTime = step.transit_details.departure_time.text;
                leaveTimeNum = step.transit_details.departure_time.value;
                break;
              default:
              // type = 'default';
              // id = 'default';
              // break;
            }
          } else {
            type = 'Walk';
            id = 'Walk';
            leaveTime = step.duration.text;
            leaveTimeNum = step.duration.value;
          }

          more.steps.push(step.html_instructions);
          // console.log(
          //   'initRoute(): adding curStep to returnList ' +
          //     id +
          //     ' | ' +
          //     leaveTime +
          //     ' | ' +
          //     type
          // );
          curStep = {
            _id: id,
            _leaveTime: leaveTime,
            _leaveTimeNum: leaveTimeNum,
            _type: type,
          };
          returnList.push(curStep);
        });
        returnList.push(more);

        /* Directions API doesn't include leaveTime in "WALKING" steps, so we need to calculate ourselves */
        returnList = calcWalkingTimes(returnList);

        returnList.push({ _destination: locationOfFirstEvent });
        resolve(returnList);
      })
      .catch((error) => {
        reject(error);
      });
  });
}
// const xdd = {
//   body: {
//     email: "koltonluu@gmail.com"
//   }
// }
// initReminders(xdd);
/**
 * Function initializes reminders for a user's schedule.
 *
 * @param req a JSON request body containing the user's email
 * @returns none
 *
 * ChatGPT usage: Partial
 */
async function initReminders(req, notificationCallback) {
  // console.log('called initReminders()');
  // console.log('initReminders(): req.body.email: ' + req.body.email);
  var returnList = [];

  var errorString = '';
  var schedule = await client
    .db('ScheduleDB')
    .collection('schedulelist')
    .findOne({ email: req.body.email });
  var user = await client
    .db('UserDB')
    .collection('userlist')
    .findOne({ email: req.body.email });

  var returnObject = {};
  if (user == null) {
    errorString = 'No matching email exists in user database';
    returnObject = { 
      status: 400, 
      message: errorString 
    };
    return returnObject;
  } else if (schedule == null) {
    errorString = 'No schedule associated with email';
    returnObject = { 
      status: 400, 
      message: errorString 
    };
    return returnObject;
  }
  // console.log('initReminders(): returned schedule: ' + schedule);
  // console.log(schedule.events[0].eventName);

  /* Get an array that contains the first event of each day */
  var firstEvents = [];
  var date = 'default';
  for (var i = 0; i < schedule.events.length; i++) {
    // assumes events are sorted by date
    if (schedule.events[i].startTime.split('T')[0] != date) {
      var event = {};
      event.locationOfOrigin = user.address;
      event.timeOfFirstEvent = schedule.events[i].startTime;
      event.locationOfFirstEvent = schedule.events[i].address.split(',')[0];

      firstEvents.push(event);

      date = schedule.events[i].startTime.split('T')[0];
    }
  }
  // console.log('initReminders(): returned firstEvents: ' + firstEvents.length);
  // console.log(firstEvents[1].timeOfFirstEvent);
  var trip = {};
  for (i = 0; i < firstEvents.length; i++) {
    // console.log(
    //   'planTrip inputs :' +
    //     firstEvents[i].locationOfOrigin +
    //     ' ' +
    //     firstEvents[i].locationOfFirstEvent +
    //     ' ' +
    //     firstEvents[i].timeOfFirstEvent
    // );
    trip = await planTransitTrip(
      firstEvents[i].locationOfOrigin,
      firstEvents[i].locationOfFirstEvent,
      new Date(firstEvents[i].timeOfFirstEvent)
    );
    // console.log(
    //   'initReminders(): returned trip: ' +
    //     trip +
    //     ' ' +
    //     trip.routes[0].legs[0].steps[0].travel_mode
    // );
    /* fields for object to be returned to frontend */
    const reminder = {
      date: 'default',
      leaveTime: 'default',
      leaveTime_iso: 'default',
      firstBus: {
        type: 'default',
        id: 'default',
        stopNumber: 'default',
        leaveTime: 'default',
      },
    };

    /* Find first instance of bus or skytrain */
    var step = {};
    var travelMode = '';
    for (var j = 0; j < trip.routes[0].legs[0].steps.length; j++) {
      step = trip.routes[0].legs[0].steps[j];
      travelMode = step.travel_mode;

      if (travelMode == 'TRANSIT') {
        switch (step.transit_details.line.vehicle.name) {
          case 'Bus':
            reminder.firstBus.type = 'Bus';
            reminder.firstBus.id = step.transit_details.line.short_name;
            reminder.firstBus.stopNumber =
              step.transit_details.departure_stop.name;
            reminder.firstBus.leaveTime =
              step.transit_details.departure_time.text;
            break;
          case 'Subway':
            reminder.firstBus.type = 'SkyTrain';
            reminder.firstBus.id = step.transit_details.line.name;
            reminder.firstBus.leaveTime =
              step.transit_details.departure_time.text;
            break;
          default:
          // console.log('initReminders(): hit default case');
          // break;
        }
        break;
      }
    }

    reminder.date = firstEvents[i].timeOfFirstEvent.split('T')[0];
    reminder.leaveTime = trip.routes[0].legs[0].departure_time.text;
    reminder.leaveTime_iso = combineDateAndTime(
      reminder.date,
      reminder.leaveTime
    ); // DEBUG: is there are problems with time, its probably because of this

    returnList.push(reminder);
  }
  // console.log('initReminders(): returned returnList: ' + returnList.length);
  // for (var i = 0; i < returnList.length; i++) {
  //   console.log("initReminders(): returned returnList: " + returnList[i].date + " " + returnList[i].leaveTime);
  // }
  // client.db('TripDB').collection('triplist').insertOne({email: req.body.email, trips: returnList}).then((result) => {
  //   console.log("planned trips for whole schedule");
  // });

  var cronTasks = [];
  var cronTimek = '';
  for (i = 0; i < returnList.length; i++) {
    if (returnList[i].firstBus.type == 'Bus') {
      // make this cleaner by changing case statement to only look for buses (RTTI API doesnt support skytrains)
      for (var k = 1; k < 6; k++) {
        cronTimek = isoToCron(returnList[i].leaveTime_iso, k);
        cronTasks[k - 1] = cron.schedule(cronTimek, () => {
          checkLiveTransitTime(
            req.body.email,
            returnList[i].firstBus.id,
            returnList[i].firstBus.stopNumber,
            returnList[i].firstBus.leaveTime,
            notificationCallback
          ).then((ret) => {
            if (ret) {
              // deschedule other checks if a notification is already sent
              cronTasks.forEach((task) => {
                task.stop();
              });
            }
          });
        });
      }
    }
  }
  returnObject = {
    status: 200,
    message: 'Reminders initialized',
  }
  return returnObject;
}
// const xdding = {
//   body: {
//     userEmail: "leonguo@gmail.com",
//     friendEmail: "koltonluu@gmail.com",
//     date: "2023-11-03"
//   }
// }
// initRouteWithFriends(xdding);

/**
 * Generates a commute route that can be shared by a user and their friend.
 *
 * @param {*} userEmail
 * @param {*} friendEmail
 * @param {*} date
 * @returns An array of object formatted identically to the array returned by initRoute()
 *
 * ChatGPT usage: Partial
 */
async function initRouteWithFriends(userEmail, friendEmail, date) {
  // var userEmail = req.body.userEmail;
  // var friendEmail = req.body.friendEmail;
  // var date = req.body.date;
  console.log('ROUTESWITHFRIENDS');
  console.log(userEmail + ' ' + friendEmail + ' ' + date);
  var error = false;
  var errorMessage = '';

  var schedule_user = await client
    .db('ScheduleDB')
    .collection('schedulelist')
    .findOne({ email: userEmail });
  if (schedule_user == null) {
    // console.log(
    //   'initRouteWithFriends(): no matching user schedule exists in schedule database'
    // );
    error = true;
    errorMessage = 'No matching user schedule exists in schedule database';
  }
  var schedule_friend = await client
    .db('ScheduleDB')
    .collection('schedulelist')
    .findOne({ email: friendEmail });
  if (schedule_friend == null) {
    // console.log(
    //   'initRouteWithFriends(): no matching friend schedule exists in schedule database'
    // );
    error = true;
    errorMessage = 'No matching friend schedule exists in schedule database';
  }

  var user = await client
    .db('UserDB')
    .collection('userlist')
    .findOne({ email: userEmail });
  if (user == null) {
    // console.log(
    //   'initRouteWithFriends(): no matching user email exists in user database'
    // );
    error = true;
    errorMessage = 'No matching user email exists in user database';
  }
  var friend = await client
    .db('UserDB')
    .collection('userlist')
    .findOne({ email: friendEmail });
  if (friend == null) {
    // console.log(
    //   'initRouteWithFriends(): no matching friend email exists in user database'
    // );
    error = true;
    errorMessage = 'No matching friend email exists in user database';
  }

  return new Promise((resolve, reject) => {
    if (error) {
      reject(errorMessage);
    }
    // determine when to arrive to campus
    var timeOfFirstEvent_user = '';
    var locationOfFirstEvent_user = '';
    var locationOfOrigin_user = user.address;
    var timeOfFirstEvent_friend = '';
    var locationOfFirstEvent_friend = '';
    var locationOfOrigin_friend = friend.address;
    for (var i = 0; i < schedule_user.events.length; i++) {
      // assumes events are sorted by date
      // console.log("initRoute(): " + schedule.events[i].startTime + " " + date);
      if (schedule_user.events[i].startTime.split('T')[0] == date) {
        // console.log("initRoute(): startTime " + schedule.events[i].startTime);
        timeOfFirstEvent_user = schedule_user.events[i].startTime;
        locationOfFirstEvent_user = schedule_user.events[i].address;
        locationOfFirstEvent_user = locationOfFirstEvent_user.split(',')[0];
        break;
      }
    }
    if (timeOfFirstEvent_user == '') {
      // console.log(
      //   'initRouteWithFriends(): no matching date exists in user schedule'
      // );
      reject('No matching date exists in user schedule');
    }

    for (i = 0; i < schedule_friend.events.length; i++) {
      // assumes events are sorted by date
      // console.log("initRoute(): " + schedule.events[i].startTime + " " + date);
      if (schedule_friend.events[i].startTime.split('T')[0] == date) {
        // console.log("initRoute(): startTime " + schedule.events[i].startTime);
        timeOfFirstEvent_friend = schedule_friend.events[i].startTime;
        locationOfFirstEvent_friend = schedule_friend.events[i].address;
        locationOfFirstEvent_friend = locationOfFirstEvent_friend.split(',')[0];
        break;
      }
    }
    if (timeOfFirstEvent_friend == '') {
      // console.log(
      //   'initRouteWithFriends(): no matching date exists in friend schedule'
      // );
      reject('No matching date exists in friend schedule');
    }

    // const twoHoursInMilliseconds = 2 * 60 * 60 * 1000;
    // const timeDifference = Math.abs(timeOfFirstEvent_user - timeOfFirstEvent_friend);
    // if (timeDifference > twoHoursInMilliseconds) {
    //   reject("time gap too big");
    // }
    var meetingPoint = getMeetingPoint(
      locationOfOrigin_user,
      locationOfOrigin_friend
    );

    var timeOfFirstEvent = '';
    var locationOfFirstEvent = '';
    if (new Date(timeOfFirstEvent_user) < new Date(timeOfFirstEvent_friend)) {
      timeOfFirstEvent = timeOfFirstEvent_user;
      locationOfFirstEvent = locationOfFirstEvent_user;
    } else {
      timeOfFirstEvent = timeOfFirstEvent_friend;
      locationOfFirstEvent = locationOfFirstEvent_friend;
    }

    console.log('made it here');

    planTransitTrip(
      meetingPoint,
      locationOfFirstEvent,
      new Date(timeOfFirstEvent)
    )
      .then((trip) => {
        // to be added to end of returnList
        var curStep1 = {
          _id: 'Walk',
          _leaveTime: trip.routes[0].legs[0].steps[0].duration.text,
          _leaveTimeNum: trip.routes[0].legs[0].steps[0].duration.value,
          _type: 'Walk',
        };
        var curStep2 = {
          _id: trip.routes[0].legs[0].steps[1].transit_details.line.short_name,
          _leaveTime:
            trip.routes[0].legs[0].steps[1].transit_details.departure_time.text,
          _leaveTimeNum:
            trip.routes[0].legs[0].steps[1].transit_details.departure_time
              .value,
          _type: 'Bus',
        };

        // to be added to end of returnList.more.steps
        var arrive_time_ubc = trip.routes[0].legs[0].arrival_time.text;
        var step1 = trip.routes[0].legs[0].steps[0].html_instructions;
        var step2 = trip.routes[0].legs[0].steps[1].html_instructions;

        var departureTimeFromStation =
          trip.routes[0].legs[0].departure_time.text;
        console.log(
          'initRouteWithFriends(): departureTimeFromStation: ' +
            departureTimeFromStation
        );
        var departureTimeFromStation_iso = combineDateAndTime(
          date,
          departureTimeFromStation
        );
        console.log(
          'initRouteWithFriends(): departureTimeFromStation iso: ' +
            departureTimeFromStation_iso
        );
        var azureTime = new Date(departureTimeFromStation_iso);
        var azureTimeToPST = azureTime.setHours(azureTime.getHours() + 8);
        
        planTransitTrip(
          locationOfOrigin_user, 
          meetingPoint, 
          new Date(azureTimeToPST)
        )
          .then((trip) => {
            console.log(
              'initRoute(): returned trip: ' +
                trip +
                ' ' +
                trip.routes[0].legs[0].steps[0].travel_mode
            );
            /* fields for object to be returned to frontend */
            var id = '';
            var leaveTime = '';
            var leaveTimeNum = '';
            var type = '';
            var travelMode = '';
            var more = {};
            var curStep = {}; // maybe properly define this object later
            var returnList = [];

            more.distance = trip.routes[0].legs[0].distance.text;
            more.duration = trip.routes[0].legs[0].duration.text;
            more.arrival_time = arrive_time_ubc;
            more.departure_time = trip.routes[0].legs[0].departure_time.text;
            more.steps = [];

            trip.routes[0].legs[0].steps.forEach((step, stepIndex) => {
              travelMode = step.travel_mode;

              if (travelMode == 'TRANSIT') {
                switch (step.transit_details.line.vehicle.name) {
                  case 'Bus':
                    type = 'Bus';
                    id = step.transit_details.line.short_name;
                    leaveTime = step.transit_details.departure_time.text;
                    leaveTimeNum = step.transit_details.departure_time.value;
                    break;
                  case 'Subway':
                    type = 'SkyTrain';
                    id = step.transit_details.line.name;
                    leaveTime = step.transit_details.departure_time.text;
                    leaveTimeNum = step.transit_details.departure_time.value;
                    break;
                  default:
                }
              } else {
                type = 'Walk';
                id = 'Walk';
                leaveTime = step.duration.text;
                leaveTimeNum = step.duration.value;
              }

              more.steps.push(step.html_instructions);
              console.log(
                'initRoute(): adding curStep to returnList ' +
                  id +
                  ' | ' +
                  leaveTime +
                  ' | ' +
                  type
              );
              curStep = {
                _id: id,
                _leaveTime: leaveTime,
                _leaveTimeNum: leaveTimeNum,
                _type: type,
              };
              returnList.push(curStep);
            });
            returnList.push(curStep1);
            returnList.push(curStep2);

            returnList.forEach((element) => {
              console.log(
                'initRouteWithFriends(): returnList: ' +
                  element._id +
                  ' ' +
                  element._leaveTime +
                  ' ' +
                  element._type
              );
            });
            returnList.push(more);
            returnList[returnList.length - 1].steps.push(step1);
            returnList[returnList.length - 1].steps.push(step2);

            /* Directions API doesn't include leaveTime in "WALKING" steps, so we need to calculate ourselves */
            returnList = calcWalkingTimes(returnList);

            returnList.push({ _destination: locationOfFirstEvent });
            resolve(returnList);

            returnList.forEach((element) => {
              console.log(
                'initRouteWithFriends(): returnList: ' +
                  element._id +
                  ' ' +
                  element._leaveTime +
                  ' ' +
                  element._type
              );
            });
          })
          .catch((error) => {
            console.log(error);
            // reject(error);
          });
      })
      .catch((error) => {
        console.log(error);
        reject(error);
      });
  });
}

/**
 * Calculates the latitude and logitude coordinates of an address
 *
 * @param address
 * @returns an array of coordinates [latitude, longitude]. The coordinates have a maximum of 5 digits after the decimal.
 *
 * ChatGPT usage: Partial
 */
function getLatLong(address) {
  return new Promise((resolve, reject) => {
    var lat = 0; // default value
    var long = 0; // default value
    var coords = [lat, long];

    var apiKey_geo =
      'AAPK3c726265cc41485bb57c5512e98cf912OLoJQtidjOlcqjdpa0Pl773UqNoOYfwApr6ORYd8Lina8_K0sEbdcyXsNfHFqLKE';
    var authentication_geo = ApiKeyManager.fromKey(apiKey_geo);

    // console.log('getLatLong: calling geocode api with address: ');
    geocode({
      address,
      authentication: authentication_geo,
    })
      .then((response) => {
        // x: longitude, y: latitude
        // console.log(response.candidates[0].location); // => { x: -77.036533, y: 38.898719, spatialReference: ... }
        lat = response.candidates[0].location.y;
        long = response.candidates[0].location.x;
        // console.log(
        //   'helper function getLatLong() output: ' +
        //     coords +
        //     ' ' +
        //     lat +
        //     ' ' +
        //     long
        // );
        coords[0] = lat.toFixed(5);
        coords[1] = long.toFixed(5);
        resolve(coords);
      })
      .catch((error) => {
        // console.log(error + "ertwetrwe");
        reject(error);
      });
  });
}

// planTransitTrip('bellingham', 'UBC', new Date()).then(() => {
//   console.log('done');
// }).catch((error) => {
//   console.log("error haha");
//   console.log(error);
// });
// getLatLong('erthbwerhjk, vancouver, BC, canada').then((coords) => {
// console.log('done');
// console.log(coords);
// }).catch((error) => {
// console.log("error");
// console.log(error);
// });


/**
 * Function to check if the address is in BC, Canada
 * 
 * @param {*} address 
 * @returns 
 * 
 * ChatGPT usage: Yes
 */
async function checkAddressInBC(address) {
  const bcLatitude = 49.701// Latitude for the center of BC
  const bcLongitude = -123.1552; // Longitude for the center of BC
  const radius = 0.8; // Radius in kilometers (adjust as needed)

  const apiKey = 'AIzaSyBVsUyKxBvRjE0XdooMuoDrpAfu1KO_2mM';
  const encodedAddress = encodeURIComponent(address);
  const apiUrl = `https://maps.googleapis.com/maps/api/geocode/json?address=${encodedAddress}&key=${apiKey}`;

  return new Promise((resolve, reject) => {
    https.get(apiUrl, (res) => {
      let data = '';
      res.on('data', (chunk) => {
        data += chunk;
      });
      res.on('end', () => {
        const response = JSON.parse(data);
        if (response.status === 'OK') {
          for (const result of response.results) {
            const lat = result.geometry.location.lat;
            const lng = result.geometry.location.lng;
            // console.log('Latitude: ', lat);
            // console.log('Longitude: ', lng);
            
            // Calculate the distance between the address and BC center
            const distance = calcDist(lat, lng, bcLatitude, bcLongitude);
            // console.log('Distance: ', distance);

            if (distance <= radius) {
              resolve(true); // Address is within the specified radius of BC
            }
          }
        }
        resolve(false); // Address is not within the specified radius of BC
      });
    }).on('error', () => {
      // console.error('Error:', err);
      resolve(false);
    });
  });
}


/**
 * Generates a commute route using Google's Directions API
 *
 * @param {*} origin      address where the commute starts
 * @param {*} destination address where the commute ends
 * @param {*} arriveTime  time at which the commute should end
 * @returns               JSON response from Google Directions API
 *
 * ChatGPT usage: Partial
 */
async function planTransitTrip(origin, destination, arriveTime) {
  return new Promise((resolve, reject) => {
    // Check if the origin is in BC, Canada
    checkAddressInBC(origin).then((result) => {
      if (!result) {
        reject('Origin address must be in British Columbia, Canada');
      }
      checkAddressInBC(destination).then((result) => {
        if (!result) {
          reject('Destination address must be in British Columbia, Canada');
        }
        const apiUrl = 'https://maps.googleapis.com/maps/api/directions/json';
        const apiKey = 'AIzaSyBVsUyKxBvRjE0XdooMuoDrpAfu1KO_2mM';
    
        const params = new URLSearchParams({
          origin,
          destination,
          mode: 'transit',
          arrival_time: Math.floor(arriveTime.getTime() / 1000),
          key: apiKey,
        });
    
        const url = `${apiUrl}?${params.toString()}`;
    
        const request = https.get(url, (response) => {
          let data = '';
          // console.log('planTransitTrip(): request sent');
    
          response.on('data', (chunk) => {
            data += chunk;
          });
    
          response.on('end', () => {
            // console.log('planTransitTrip() return');
            // console.log('--------------------------------------------------------');
            var routes = JSON.parse(data).routes;
            if (routes.length <= 0) {
              // console.log('No routes found.');
              reject('No routes found. Please check if you used a valid address.');
            }
            // if (routes.length > 0) {
            //   // Process each route
            //   routes.forEach((route, index) => {
            //     // Access route information such as summary, distance, duration, steps, etc.
            //     console.log(`Route ${index + 1}:`);
            //     console.log(`Summary: ${route.summary}`);
            //     console.log(`Distance: ${route.legs[0].distance.text}`);
            //     console.log(`Duration: ${route.legs[0].duration.text}`);
            //     console.log('Steps:');
            //     route.legs[0].steps.forEach((step, stepIndex) => {
            //       console.log(`Step ${stepIndex + 1}: ${step.html_instructions}`);
            //     });
            //     console.log('-----------------------');
            //   });
            // } 
            // console.log('--------------------------------------------------------');
            // console.log(data);
            // console.log(JSON.parse(data));
            // console.log('--------------------------------------------------------');
            resolve(JSON.parse(data));
          });
        });
        request.on('error', (err) => {
          // console.log('Error: ' + err.message);
          reject('Translink server error: ' + err.message);
        });
      });
    }).catch((error) => {
      // console.log("Google server error: " + error);
      reject("Google server error: " + error);
    });
  });
}

// ChatGPT usage: Yes
function calcDist(x1, y1, x2, y2) {
  var a = x1 - x2;
  var b = y1 - y2;

  return Math.sqrt(a * a + b * b);
}

/**
 * Determine whether to take 99 B-Line or R4 based on addresses of user and friend
 * @param {*} address1
 * @param {*} address2
 *
 * ChatGPT usage: No
 */
function getMeetingPoint(address1, address2) {
  var addressCoords_user = getLatLong(address1);
  var addressCoords_friend = getLatLong(address2);
  var distToCommercial_user = calcDist(
    addressCoords_user[0],
    addressCoords_user[1],
    49.2629,
    -123.0684
  );
  var distToJoyce_user = calcDist(
    addressCoords_user[0],
    addressCoords_user[1],
    49.2382,
    -123.0312
  );
  var distToCommercial_friend = calcDist(
    addressCoords_friend[0],
    addressCoords_friend[1],
    49.2629,
    -123.0684
  );
  var distToJoyce_friend = calcDist(
    addressCoords_friend[0],
    addressCoords_friend[1],
    49.2382,
    -123.0312
  );
  var meetingPoint = '';
  if (
    (distToCommercial_friend + distToCommercial_user) / 2 <
    (distToJoyce_friend + distToJoyce_user) / 2
  ) {
    meetingPoint = 'Commercial Broadway Station';
  } else {
    meetingPoint = 'Joyce-Collingwood Station';
  }
  return meetingPoint;
}

/**
 * Impute missing data in 'Walk' steps of a transit route
 * @param {*} commuteInfo
 *
 * ChatGPT usage: No
 */
function calcWalkingTimes(commuteInfo) {
  var returnList = commuteInfo;
  for (var i = 0; i < returnList.length - 1; i++) {
    if (i === 0 && returnList[i]._type == 'Walk') {
      returnList[i]._leaveTime =
        returnList[returnList.length - 1].departure_time;
      returnList[i]._leaveTimeNum = timeToTimestamp(
        returnList[returnList.length - 1].departure_time
      );
    } else if (i == returnList.length - 2 && returnList[i]._type == 'Walk') {
      returnList[i]._leaveTime = returnList[returnList.length - 1].arrival_time;
      returnList[i]._leaveTimeNum = timeToTimestamp(
        returnList[returnList.length - 2].arrival_time
      );
    } else if (returnList[i]._type == 'Walk') {
      // assumes 'type' for next array entry is either "Bus" or "SkyTrain"
      returnList[i]._leaveTime = timestampToTime(
        returnList[i + 1]._leaveTimeNum - returnList[i]._leaveTimeNum
      );
      returnList[i]._leaveTimeNum =
        returnList[i + 1]._leaveTimeNum - returnList[i]._leaveTimeNum;
    }
    // console.log('updated leavetime: ' + returnList[i]._leaveTime);
  }
  return returnList;
}

// ChatGPT usage: Yes
function timeToTimestamp(timeString) {
  const date = new Date(timeString);
  return Math.floor(date.getTime() / 1000);
}

// ChatGPT usage: Yes
function timestampToTime(timestamp) {
  const date = new Date(timestamp * 1000);
  return date.toLocaleTimeString('en-US', {
    hour: '2-digit',
    minute: '2-digit',
  });
}
// combineDateAndTime('2021-03-01', '12:00 PM');
// ChatGPT usage: Yes
function combineDateAndTime(dateString, timeString) {
  // Convert the time to 24-hour format and add seconds
  // console.log('inputs: ' + dateString + ' ' + timeString);
  // const timeComponents = timeString.match(/(\d+):(\d+) (A|P)M/);

  const timeComponents = timeString.match(/(\d+):(\d+)\s*(A|P)\s*M/);
  // const timeComponents = timeString.match(/(\d+):(\d+)\s*(A|P)\s*M/);
  // if (!timeComponents) {
  //   throw new Error('Invalid time format');
  // }

  var hours = parseInt(timeComponents[1], 10);
  var minutes = parseInt(timeComponents[2], 10);
  var isPM = timeComponents[3].toLowerCase() === 'p';

  // Adjust hours for PM
  if (isPM && hours < 12) {
    hours += 12;
  }

  const isoDateTimeString = `${dateString}T${hours
    .toString()
    .padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:00`;
  // console.log('output: ' + isoDateTimeString);
  const date = new Date(isoDateTimeString);

  // if (isNaN(date)) {
  //   throw new Error('Invalid date or time format');
  // }

  return date.toISOString();
}

// ChatGPT usage: Yes
function isoToCron(isoString, minutesBefore) {
  const date = new Date(isoString);

  // Subtract minutes
  const updatedMinutes = date.getMinutes() - minutesBefore;

  // Handle negative minutes
  const minutes = updatedMinutes < 0 ? 60 + updatedMinutes : updatedMinutes;

  // Adjust the hour if necessary
  const hours = updatedMinutes < 0 ? date.getHours() - 1 : date.getHours();

  const cronString = `${date.getSeconds()} ${minutes} ${hours} ${date.getDate()} ${
    date.getMonth() + 1
  } *`;

  return cronString;
}

// ChatGPT usage: Yes
function compareTimeStrings(timeStr1, timeStr2) {
  // Convert both strings to lowercase and remove spaces
  const formattedTimeStr1 = timeStr1.toLowerCase().replace(/\s/g, '');
  const formattedTimeStr2 = timeStr2.toLowerCase().replace(/\s/g, '');

  return formattedTimeStr1 === formattedTimeStr2;
}

module.exports = {
  app,
  sendNotification,
  findUserToken,
  connectToDatabase,
  startSSLServer,
  sslServer,
  stopSSLServer,
  checkLiveTransitTime,
};
