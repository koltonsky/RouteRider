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
const axios = require('axios')

const user = require('./routes/user.js')
const schedule = require('./routes/schedule.js');
const { get } = require('http');
const { time } = require('console');

// MongoDB connection setup
const uri = 'mongodb://0.0.0.0:27017'; // Replace with your MongoDB connection string
const client = new MongoClient(uri);

// returns a list of users and events which match up on the first day
async function findUsers(userEmail) {
    var userSchedule = await client.db('ScheduleDB').collection('schedulelist').findOne({email: userEmail});
    if (!userSchedule) {
        console.log('User not found or schedule is empty.');
        return;
      }
    
    console.log(userSchedule);

    var matchingEvents = [];
    return "print test";
}

async function getFirstEventsOfEachDay(userEmail) {
    try {
        await client.connect();

        const userSchedule = await client.db('ScheduleDB').collection('schedulelist').findOne({ email: userEmail });

        if (!userSchedule) {
            console.log('User not found or schedule is empty.');
            return { events: [] };
        }

        // Aggregate the events to find the first event of each day
        const pipeline = [
            {
                $match: { email: userEmail },
            },
            {
                $unwind: '$events',
            },
            {
                $sort: { 'events.startTime': 1 }, // Sort events by start time in ascending order
            },
            {
                $group: {
                    _id: {
                        date: {
                            $dateToString: { format: '%Y-%m-%d', date: { $toDate: '$events.startTime' } }
                        },
                    },
                    event: { $first: '$events' },
                },
            },
        ];

        const firstEvents = await client.db('ScheduleDB').collection('schedulelist').aggregate(pipeline).toArray();
        const reversedFirstEvents = firstEvents.reverse();

        console.log('First events of each day:');
        console.log(reversedFirstEvents);

        return { events: reversedFirstEvents };
    } catch (err) {
        console.error('Error:', err);
        return { events: [] }; // Return an empty array in case of an error
    } finally {
        await client.close();
    }
}


  // new function: takes in useremail and firstevents, 
  async function findMatchingCommuters(userEmail) {
    await client.connect();
    const userFirstEvents = getFirstEventsOfEachDay(userEmail);

    // Construct a query to find all schedules except the user's schedule
    const query = { email: { $ne: userEmail } };

    // Find schedules that match the query
    const schedulesExcludingUser = await client.db('ScheduleDB').collection('schedulelist').find(query).toArray();

    console.log('Schedules excluding the user:');
    console.log(schedulesExcludingUser);

    console.log(userFirstEvents);
  }
  


  //getFirstEventsOfEachDay("koltonluu@gmail.com");
  findMatchingCommuters("koltonluu@gmail.com");
  //findUsers("koltonluu")

module.exports = {
    findUsers,
    getFirstEventsOfEachDay,
  };
  
