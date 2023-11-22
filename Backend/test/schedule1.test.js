const { MongoClient } = require('mongodb');
const supertest = require('supertest');
const app = require('../server');
const request = supertest(app);

// Mock MongoDB
jest.mock('mongodb');

let client;

beforeAll(async () => {
  // Set up MongoDB connection before tests
  try {
    const uri = 'mongodb://0.0.0.0:27017';
    client = new MongoClient(uri);
    await client.connect();
    console.log("Connected to MongoDB");
  } catch (error) {
    console.error('MongoDB Connection Error:', error);
  }
});

afterAll(async () => {
  // Close MongoDB connection after all tests
  if (client) {
    await client.close();
  }
});

describe('Create New Schedule', () => {
  let scheduleData;

  beforeEach(() => {
    scheduleData = {
      email: 'user@example.com',
      // Add other schedule data properties as needed
    };
  });

  afterEach(async () => {
    // Clean up the test data after each test
    // Assuming you have already connected to the MongoDB client
    const collection = client.db('ScheduleDB').collection('schedulelist');
    await collection.deleteOne({ email: scheduleData.email });
  });

  test('POST /api/schedulelist should create a new schedule successfully', async () => {
    const insertOneMock = jest.spyOn(client.db('ScheduleDB').collection('schedulelist'), 'insertOne');
    insertOneMock.mockResolvedValue({ result: { ok: 1 } });

    const res = await request
      .post('/api/schedulelist')
      .send(scheduleData);

    expect(res.status).toBe(201);
    expect(res.body.message).toBe('Schedule created successfully');

    // Restore the mock
    insertOneMock.mockRestore();
  });

  test('POST /api/schedulelist should return an error for an existing schedule', async () => {
    // Mocking an error during the database insertion
    const insertOneMock = jest.spyOn(client.db('ScheduleDB').collection('schedulelist'), 'insertOne');
    insertOneMock.mockRejectedValue(new Error('MongoDB duplicate key error'));

    const res = await request
      .post('/api/schedulelist')
      .send(scheduleData);

    expect(res.status).toBe(409);
    expect(res.body.message).toBe('Schedule with this email already exists');

    // Restore the mock
    insertOneMock.mockRestore();
  });

  test('POST /api/schedulelist should handle server errors during creation', async () => {
    // Mocking an error during the database insertion
    const insertOneMock = jest.spyOn(client.db('ScheduleDB').collection('schedulelist'), 'insertOne');
    insertOneMock.mockRejectedValue(new Error('Mocked MongoDB insertion error'));

    try {
      const res = await request
        .post('/api/schedulelist')
        .send(scheduleData);

      console.log('Response:', res.status, res.body);

      expect(res.status).toBe(500);
      expect(res.body.error).toBe('Internal server error');
    } catch (error) {
      console.error('Test error:', error);
    } finally {
      // Restore the original implementation of the mocked method
      insertOneMock.mockRestore();
    }
  });
});
