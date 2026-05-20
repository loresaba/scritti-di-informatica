# include <assert.h>
# include <math.h>
# include <stdarg.h>
# include <stdbool.h>
# include <stdio.h>
# include <stdlib.h>
# include <string.h>
# include <time.h>

/* ========================= Simulation parameters ========================== */
/* MODIFY THIS PARAMETERS AS YOU NEED FOR THE SIMULATION */

/* Simulation */
#define HORIZON 5000000 // Simulation time limit.
#define SIM_RUNS 5		// Number of runs in each simulation.

/* Random number */
#define SEED -1 		// -1 for a random seed.

/* Packet */
#define DATA_DUR 4
#define ACK_DUR 1
#define SIFS_DUR 2
#define DIFS_DUR 4

/* Node */
#define NODE_NUM 10
#define RANGE 3         // Trasmission range of a node.
#define QUEUE_CAPACITY 10
#define GEN_PROB 0.005	// Probabily to generate a new packet.
#define ACK_TIMEOUT 8

/* Contention Window */
#define CW_MIN 15
#define CW_MAX 1023
#define SLOT_TIME 1     // Duration of one time slot.

/* Reinforcement learning */
#define RL_EPSILON 0.05        // Probability to explore.
#define RL_ALPHA 0.10          // Learning rate.
#define ARMS_NUM 6
#define OPTIM_INIT_VALUES 60.0 // Optimistic initial value.

/* Monitored area */
#define MAREA_WIDTH  10
#define MAREA_HEIGHT 10

/* Reiforcement Learning */
#define USE_EPSILON_GREEDY  // Use the epsilon-greedy RL technique.
				            // Comment for disable it.
/* Print and Debug */
//#define PRINT_MAREA         // Print the monitored area.
				            // Comment for disable it.
//#define PRINT_NEIGHBORS 	  // Print the neighbor lists
				            // Comment for disable it.
//#define PRINT_LOG       	  // Print the log of the simulation
				            // Comment for disable it.
#define PRINT_STATS		    // Print the results of the simulation. 
				            // Comment for disable it.

#define PRINT_SIM_INFO // Print the current simulation running infos.
				       // Comment for disable it.

/* =============================== Components =============================== */

/* Represent the types of packet. */
typedef enum {
	DATA, ACK
} PacketType;

/* Represent the packet send by nodes. */
typedef struct {
	int id;
	PacketType type;
	
	int srcId;
	int dstId;
	int duration;
	int createTime;

	int refcount; // Track how many nodes hold this packet.
} Packet;

/* Represent an entry in the packet queue linked lists. */
typedef struct PacketEntry {
	Packet *packet;
	struct PacketEntry *next;
} PacketEntry;

/* Represent the queue of packets. */
typedef struct {
	PacketEntry *head;	
	PacketEntry *tail;	
	int count;
	int capacity;
} PacketQueue;

/* Represent the states of a node. */
typedef enum {
	IDLE, 
	WAIT_SIFS,
	WAIT_DIFS,
	WAIT_ACK,
	BACKOFF,
	TRANSMITTING,
	RECEIVING,
} NodeState;

/* Represent the node. */
typedef struct {
	int id;
	NodeState state;

	int timer;
	int cw;

	double qValues[ARMS_NUM]; // Estimate values for each CW size;
	int lastAction;           // Index of the last chosen arm.

	PacketQueue input;  // Packets received.
	PacketQueue output; // Packets to be sent.
} Node;

/* Represent a dynamic array of integers. */
typedef struct {
	int *data;
	int count;
	int capacity;
} IntArray;

/* Represent the monitored area. */
typedef struct {
	int width;
	int height;
	int *nodes;
} Marea;

/* Represent all the configurable parameters of the simulator. */
typedef struct SimConf {
    int simRuns;
	int seed;
	int horizon;
	
	int dataDur;
	int ackDur;
	int sifsDur;
	int difsDur;
	
	int nodeNum;
	int range;
	double genProb;
	int ackTimeout;
	
	int cwMin;
	int cwMax;
	int slotTime;

	double epsilon;
	double alpha;
	
	int mareaWidth;
	int mareaHeight;

    bool useEpsilonGreedy;
	
	bool printMarea;
	bool printNeighbors;
	bool printLog;
	bool printStats;
} SimConf;

/* Represent a statistical metric. */
typedef struct {
	double mean;
	double var;
	int n;
} Stat;

/* Represent a 2D point on the plot. */
typedef struct {
    double x, y;
} Point;

/* Represent a series of data on the plot. */
typedef struct {
    char *label;
    Point *points;
    int count;
    int capacity;
} PlotSeries;

/* Represent a plot. */
typedef struct {
    char *title;
    char *xlabel;
    char *ylabel;

    PlotSeries *series;
    int seriesCount;
    int seriesCap;
} Plot;

/* Represent the simulation state. */
typedef struct {
	char *name;

	// Simulation parameters.
	SimConf params;
	int packetId; // First packet ID.
	int t;		  // Simulation time.	

	// Environment state.
	Node **nodes;		 // Nodes.
	Marea marea;		 // Monitored area.
	IntArray *neighbors; // Neighbors.
	
	// Performace metrics.
	int packetGenerated;
	int packetSent;
	int packetReceived;
	int packetAcked;
	int packetDropped;
	int collisions;

	Stat pdr;
	Stat throughput;
	Stat collRate;
	Stat avgLatencyRun;
	Stat avgLatencyTotal;
} Simulator;

/* =========================== Global declaration =========================== */

Simulator sim;
Plot plot;

void simprintf(const char *fmt, ...);
void saveStatValue(Stat *stat, double v);
int getCWFromArm(int armIndex);
void selectAction(Node *node);
void rewardNode(Node *node, double reward);

void initPlot(Plot *p, char *title, char *xlabel, char *ylabel);
int addPlotSeries(Plot *p, char *label);
void addPlotData(Plot *p, int seriesIndex, double x, double y);
void generatePlot(Plot *p, const char *filename);
void freePlot(Plot *p);

/* ================================= Packet ================================= */
/* In CSMA/CA, a single transmission is heard by multiple neighbors
 * simultaneously. Instead of creating a copy of the packet for every
 * neighbor, we use a reference counting approach:
 * 1. All neighbors receive a pointer to the same packet object.
 * 2. The 'refcount' field tracks how many nodes currently hold a reference
 *    to this packet.
 * 3. When 'refcount' drops to 0, no one is using it anymore, the packet memory
 *    is freed. */

/* Increment the reference count of the packet. */
void retainPacket(Packet *p) {
	if (p != NULL) p->refcount++;
}

/* Decrement the reference count of the packet. 
 * If the 'refcount' becomes 0, it frees the memory. */
void releasePacket(Packet *p) {
	if (p == NULL) return;

	p->refcount--;

	if (p->refcount == 0) {
		free(p);
	}
}

/* Return the duration of a packet besed on its 'type'. */
int getPacketDuration(PacketType type) {
	switch(type) {
	case DATA: return sim.params.dataDur;
	case ACK:  return sim.params.ackDur;
	}
	return -1;
}

/* Initialize and allocate memory for a packet. */
Packet *createPacket(PacketType type, int send, int dest) {
	Packet *p = malloc(sizeof(Packet));
	p->id = sim.packetId;
	p->type = type;
	p->srcId = send;
	p->dstId = dest;
	p->duration = getPacketDuration(type);
	p->createTime = sim.t;
	p->refcount = 1;

	sim.packetId++;
	return p;
}

/* Return the string representation of the packet 'type'. */
char *getPacketTypeStr(PacketType type) {
	switch (type) {
	case DATA: return "DATA";
	case ACK:  return "ACK";
	}
	return NULL;
}

/* ============================== Packet Queue ============================== */

/* Initialize the packet queue with a fixed capacity. */
void initPacketQueue(PacketQueue *q, int cap) {
	q->head = NULL;
	q->tail = NULL;
	q->count = 0;
	q->capacity = cap;
}

/* Add the packet to the end of the queue. Returns true on success, returns
 * false otherwise. */
bool enqueuePacket(PacketQueue *q, Packet *p) {
	if (q->count >= q->capacity) {
		return false;
	}
	// Create a new entry.
	PacketEntry *entry = malloc(sizeof(PacketEntry));
	entry->packet = p;
	entry->next = NULL;

	// Link the new entry to the end of the queue.
	if (q->tail != NULL) {
		q->tail->next = entry;
	}
	q->tail = entry;

	if (q->head == NULL) {
		q->head = entry;
	}

	q->count++;
	return true;
}

/* Remove and return the packet at the front of the queue. 
 * The function also frees the memory of the PacketEntry dequeued.
 * It returns the dequeued packet if any, returns NULL otherwise. */
Packet *dequeuePacket(PacketQueue *q) {
	if (q->head == NULL) return NULL;

	PacketEntry *entry = q->head;
	Packet *packet = entry->packet;

	q->head = q->head->next;

	if (q->head == NULL) {
		q->tail = NULL;
	}

	free(entry);
	q->count--;

	return packet;
}

/* Add the packet to the front of the queue. Returns true on success, returns
 * false otherwise. 
 * This functions is used to place high priority packet to the front of the
 * queue. */
bool prependPacket(PacketQueue *q, Packet *p) {
	if (p->type != ACK && q->count >= q->capacity) {
		return false;
	}

	PacketEntry *entry = malloc(sizeof(PacketEntry));
	entry->packet = p;
	entry->next = q->head;

	q->head = entry;

	if (q->tail == NULL) {
		q->tail = entry;
	}

	q->count++;
	return true;
}

/* Return the packet at the front of the queue. */
Packet *peekPacket(PacketQueue *q) {
	if (q->head == NULL) return NULL;
	return q->head->packet;
}

/* Return true if the queue is empty. Return false otherwise. */
bool isQueueEmpty(PacketQueue *q) {
	return (q->count == 0);
}

/* Free the memory allocated for the queue. 
 * The function frees all the allocated memory for the packet entries and
 * packets inside them. */
void freePacketQueue(PacketQueue *q) {
	while (!isQueueEmpty(q)) {
		Packet *p = dequeuePacket(q);
		if (p != NULL) releasePacket(p);
	}
}

/* Reset the packet queue by removing all the packets and starting a fresh
 * queue. */
void resetPacketQueue(PacketQueue *q) {
	freePacketQueue(q);
	initPacketQueue(q, QUEUE_CAPACITY);
}

/* ================================== Node ================================== */

/* Initialize and allocate memory for a node. */
Node *createNode(int id) {
	Node *node = malloc(sizeof(Node));

	node->id = id;
	node->state = IDLE;
	node->cw = 0;
	node->timer = 0;

	for (int i = 0; i < ARMS_NUM; i++) {
		node->qValues[i] = OPTIM_INIT_VALUES;
	}

	node->lastAction = 0;
	node->cw = getCWFromArm(0);

	initPacketQueue(&node->input,  QUEUE_CAPACITY);
	initPacketQueue(&node->output, QUEUE_CAPACITY);
	
	return node;
}

/* Return the string representation of the node 'state'. */
char *getNodeStateStr(NodeState state) {
	switch (state) {
	case IDLE:
		return "IDLE";
	case WAIT_SIFS:
		return "WAIT_SIFS";
	case WAIT_DIFS:
		return "WAIT_DIFS";
	case WAIT_ACK:
		return "WAIT_ACK";
	case BACKOFF: 
		return "BACKOFF";
	case TRANSMITTING: 
		return "TRANSMITTING";
	case RECEIVING: 
		return "RECEIVING";
	}
	return NULL;
}

/* ============================= Monitored area ============================= */
/* The following functions create and handle the monitored area. A monitored
 * area is simply represented by a grid in a 2D space. Each cell in this grid
 * contains the node id if any, contains -1 otherwise. 
 * For example, a monitored area might be:
 * +---------------+
 * | . . . 2 . . 6 |
 * | . 0 . . . . . |
 * | 8 . 5 7 . . . | The '.' indicates
 * | . . . 9 . . . | the absence of nodes.
 * | 1 . . . . 4 3 |
 * +---------------+
 * */

/* Initialize the monitored area. */
void initMarea(Marea *marea, int w, int h) {
	marea->width = w;
	marea->height = h;
	marea->nodes = malloc(sizeof(int) * w * h);

	for (int i = 0; i < w * h; i++) marea->nodes[i] = -1;	
}

/* Free the memory allocated for monitored area. */
void freeMarea(Marea *marea) {
	free(marea->nodes);
}

/* Return true if the x,y coordinates are in the monitored area, return false
 * otherwise. */
bool isInMarea(Marea *marea, int x, int y) {
	return (x >= 0 && x < marea->width && y >= 0 && y < marea->height);
}

/* Return the id of the node in the monitored area at x,y coordinates if any,
 * return -1 otherwise. */
int getMareaNode(Marea *marea, int x, int y) {
	if (isInMarea(marea, x, y)) {
		int i = (y * marea->width) + x;
		return marea->nodes[i];
	}
	return -1;
}

/* Place the 'node' in the monitored area at x,y coordinates. 
 * The function also checks and aborts in case of out of bound coordinates. */
void placeNode(Marea *marea, Node *node, int x, int y) {
	// Check the bounds.
	if (!isInMarea(marea, x, y)) {
		fprintf(stderr, "Error: Coordinates (%d, %d) of node %d outside"
			        " the monitored area.\n", x, y, node->id);
		exit(1);
	}

	// Check if the cell is free. 
	if (getMareaNode(marea, x, y) != -1) {
		fprintf(stderr, "Cell (%d, %d) already contains a node.\n", x, y);
		exit(1);
	}
	
	// Place the node id.
	int i = (y * marea->width) + x;
	marea->nodes[i] = node->id;
}

/* Randomly place 'nodes' in the monitored area. 
 * Each node is placed to a unique x,y position in range [0, MAREA_WIDTH *
 * MAREA_HEIGHT - 1]. */
void randPlaceNodes(Marea *marea, Node **nodes, int count) {
	int totalCells = marea->width * marea->height;

	if (count > totalCells) {
		fprintf(stderr, "Error: Too many nodes for the monitored area.\n");
		exit(1);
	}

	// Create integer array [0, ..., totalCells-1].
	int pos[totalCells];

	for (int i = 0; i < totalCells; i++) {
		pos[i] = i;
	}

	// Shuffle the array of positions.	
	for (int i = totalCells - 1; i > 0; i--) {
		int j = rand() % (i + 1);
		int tmp = pos[i];
		pos[i] = pos[j];
		pos[j] = tmp;
	}

	// Place the nodes in the monitored area.
	for (int i = 0; i < count; i++) {
		int p = pos[i];

		int y = p / marea->width;
		int x = p % marea->width;

		placeNode(marea, nodes[i], x, y);
	}
}

/* Print the monitored area grid. */
void printMarea(Marea *marea, int maxId) {
	printf("Monitored Area:\n");

	// Compute the cell width using the maximum node id.
	int cellw = 1;
	while (maxId >= 10) {
		maxId /= 10;
		cellw++;
	}
	cellw++; // Add a space between cells.

	for (int y = 0; y < marea->height; y++) { 
		for (int x = 0; x < marea->width; x++) {
			// Print the node id (or '.' if there is none) and add
			// spaces to compleate the cell width.
			int nodeId = getMareaNode(marea, x, y);
			if (nodeId == -1) {
				printf("%-*s", cellw, ".");
			} else {
				printf("%-*d", cellw, nodeId);
			}
		}
		printf("\n");
	}
}

/* ============================= Integer array ============================== */

/* Initialize and allocate memory for the dynammic array of integers. */
void initIntArray(IntArray *arr) {
	arr->count = 0;
	arr->capacity = 8;
	arr->data = malloc(sizeof(int) * arr->capacity);
}

/* Add the integer to the integer array. */
void addInt(IntArray *arr, int i) {
	if (arr->count >= arr->capacity) {
		arr->capacity *= 2;
		arr->data = realloc(arr->data, sizeof(int) * arr->capacity);
	}
	arr->data[arr->count++] = i;
}

/* Free the memory allocated for the integer array. */
void freeIntArray(IntArray *arr) {
	free(arr->data);
}

/* =============================== Neighbors ================================ */
/* A neighbor array is a sequence of integer arrays contaning the IDs of
 * neighbor nodes. Where each integer array represents the set of neighbors of
 * node i. For example, a neighbor array might be:
 * +-------------------------+
 * | Node0 [15, 8, 9, 17, 3] |
 * | Node1 [19, 2]           | Node19 and Node2 are
 * |  ...  [ ... ]           | neighbors of Node1.
 * | NodeN [2, 1, 18]        |
 * +-------------------------+
 */

/* Initialize the array of 'neighbors' of size 'size' exploring the monitored
 * area. 
 * The function scans the monitored area and for each node builds an array of
 * neighbor ids reachable in its 'range'. */
void buildNeighbors(Marea *marea, IntArray *neighbors, int size, int range) {
	// Initialize the neighbor arrays.
	for (int i = 0; i < size; i++) {
		initIntArray(&neighbors[i]);
	}
	
	// Scan the monitored area.
	for (int y = 0; y < marea->height; y++) {
		for (int x = 0; x < marea->width; x++) {
			int id = getMareaNode(marea, x, y);
			if (id == -1) continue;

			// Find the neighbors (dx, dy) in range of 'node'.
			for (int dy = y - range; dy <= y + range; dy++) {
				for (int dx = x - range; dx <= x + range; dx++)
				{
					if (dy == y && dx == x) continue;
					int neigh = getMareaNode(marea, dx, dy);
					if (neigh == -1) continue;

					// Append neighbor id.
					addInt(&neighbors[id], neigh);
				}
			}
		}
	}
}

/* Free the memory allocated for the 'neighbors' array. */
void freeNeighbors(IntArray *neighbors, int size) {
	for (int i = 0; i < size; i++) {
		freeIntArray(&neighbors[i]);
	}
}

/* Print the 'neighbor' arrays. */
void printNeighbors(IntArray *neighbors, int size) {
	if (size == 0) {
		printf("No neighbor info.\n");
		return;
	}

	printf("Neighbors:\n");

	for (int id = 0; id < size; id++) {
		printf("Node %d: [", id);
		for (int k = 0; k < neighbors[id].count; k++) {
			printf("%d", neighbors[id].data[k]);
			if (k < neighbors[id].count - 1) printf(", ");
		}
		printf("]\n");
	}
}

/* =========================== Node communication =========================== */
/* The following functions handle the communication between nodes. These
 * include how nodes generate, send and receive packets from neighbors. */

/* Broadcast a packet to all neighbors. */
void sendPacket2Neighbors(Packet *p) {
	if (p == NULL) return;

	// Loop over all the neighbors.
	for (int i = 0; i < sim.neighbors[p->srcId].count; i++) {
		int neighId = sim.neighbors[p->srcId].data[i];
		Node *neigh = sim.nodes[neighId];
		
		// Add the packet to the neighbor queue.
		if (enqueuePacket(&neigh->input, p)) retainPacket(p);
		else sim.packetDropped++;
	}
}

/* ============================ CSMA/CA protocol ============================ */
/* The following functions implement the logic of the CSMA/CA protocol by
 * following these steps. The node that has a packet to send:
 * 1. Listens to the channel (carrier sense).
 * 2. If the channel is idle, the node waits an inter-frame space (DIFS). If
 *    the channel remains idle for the DIFS interval, the node transmits its
 *    data. When the packet is correctly received, the recipient responds with
 *    an acknowledgment (ACK) after waiting a short inter-frame space (SIFS).
 * 3. If the channel is busy the node keeps monitoring the channel until the
 *    current transmission ends.
 * 4. If the channel becomes idle again, each contending node enters a
 *    backoff procedure: it chooses a random number of time slots from within a
 *    contention window and starts a backoff timer. That timer ticks down only
 *    while the channel remains idle. The first node whos backoff timer reaches
 *    zero takes the channel and transmits; the others detect that transmission
 *    and freeze their timers, resuming them after the channel becomes idle
 *    again. 
 * 5. If a node’s packet collides or is not acknowledged the protocol
 *    exponentially increases the contention window and the node attempt a
 *    retrasmission.
 * */

/* Return true if the channel is free by the 'node's prospective, return false
 * otherwise. */
bool isChannelIdle(Node *node) {
	// Phisical carrier sense. Check if any neighbors is trasmitting.
	for (int i = 0; i < sim.neighbors[node->id].count; i++) {
		int neighId = sim.neighbors[node->id].data[i];
		if (sim.nodes[neighId]->state == TRANSMITTING) return false;
	}

	return true;
}

/* Increase the contention window size of the 'node'. Binary exponential
 * backoff. */
void increaseCW(Node *node) {
	node->cw = ((node->cw + 1) * 2) - 1;
	if (node->cw > sim.params.cwMax) node->cw = sim.params.cwMax;
}

/* Reset the contention window of the 'node' to the minimum. */
void resetCW(Node *node) {
	node->cw = sim.params.cwMin;
}

/* Set the 'node's state, and its releated values, based on the 'state'
 * provided input. */
void setNodeState(Node *node, NodeState state) {
	node->state = state;

	switch (state) {
	case IDLE:
		node->timer = 0;
		break;
	case WAIT_SIFS:
		node->timer = sim.params.sifsDur;
		break;
	case WAIT_DIFS:
		node->timer = sim.params.difsDur;
		break;
	case WAIT_ACK:
		node->timer = sim.params.ackTimeout;
		break;
	case BACKOFF:
		// Choose a random number from contention window [0, cw].
		node->timer = rand() % (node->cw + 1);
		break;
	case TRANSMITTING:
	case RECEIVING:
		// The calling function is expected to set the timer based on
		// the packet duration.
		break;
	}
}

/* Perform actions on the 'node' based on the provided packet. */
void processPacket(Node *node, Packet *p) {
	if (p->dstId == node->id) { // I'm the destination.
		// We finish receiving the packet.
		if (node->state == RECEIVING) {
			if (p->type == ACK) {
				simprintf("Node %d finished receiving ACK "
					  "Packet %d from Node %d\n", node->id,
					  p->id, p->srcId);
				sim.packetAcked++;
				// Discard the DATA packet still in memory. 
				// To know why, see the TRANSMITTING case in
				// the updateNode function.
				Packet *data = dequeuePacket(&node->output);
				releasePacket(data);

				// ACK received correctly.
                if (sim.params.useEpsilonGreedy) {
                    // The higher arm will receive less reward.
                    // This helps to prevent the agent for choosing only the
                    // higher arms.
                    int currCW = getCWFromArm(node->lastAction);
                    double reward = 50.0 - (currCW * 0.05);
                    if (reward < 1.0) reward = 1.0;

                    rewardNode(node, reward);
                    selectAction(node);
                } else {
                    resetCW(node);
                }

				setNodeState(node, IDLE);

			} else if (p->type == DATA) {
				simprintf("Node %d finished receiving DATA "
					  "Packet %d from Node %d\n", node->id,
					  p->id, p->srcId);
				int dur = sim.t - p->createTime;
				saveStatValue(&sim.avgLatencyRun, dur);

				sim.packetReceived++;
				// DATA received correctly. 
				// Send an ACK after SIFS.
				Packet *ack = createPacket(ACK, node->id,
						           p->srcId);
				if (prependPacket(&node->output, ack)) {
					setNodeState(node, WAIT_SIFS);
					simprintf("Node %d creates ACK Packet "
						  "%d for Node %d\n", node->id,
						  ack->id, ack->dstId);
				} else {
					releasePacket(ack);
					simprintf("Node %d queue full, cannot "
						  "ACK Packet %d\n", node->id,
						  p->id);
				}
			}
		}

		// We receive a fresh packet.
		else {
			// Start receiving the ACK packet if we are waiting it.
			if (p->type == ACK) {
				if (node->state == WAIT_ACK) {
					setNodeState(node, RECEIVING);
					node->timer = p->duration;

					simprintf("Node %d starting receiving "
						  "ACK Packet %d from Node "
						  "%d\n", node->id, p->id,
						  p->srcId);
				}
			// Start receiving the DATA packet if we are listening.
			} else if (p->type == DATA) {
				if (node->state == IDLE || node->state ==
				    WAIT_DIFS || node->state == BACKOFF) {
					setNodeState(node, RECEIVING);
					node->timer = p->duration;

					simprintf("Node %d starting receiving "
						  "DATA Packet %d from Node "
						  "%d\n", node->id, p->id,
						  p->srcId);
				}
			}
		}
	}
}

/* Main simulation step for a single 'node'. */
void updateNode(Node *node) {
	// Check if we received packets from neighbors.
	if (!isQueueEmpty(&node->input)) {
		// If we are TRANSMITTING, we are half-duplex and cannot hear.
		if (node->state == TRANSMITTING) {
			// Discard the received packets.
			resetPacketQueue(&node->input);
		// If we received two or more packet at the same time, a
		// collision occurs.
		} else if (node->input.count > 1) {
			simprintf("Collision at Node %d while %s\n", node->id,
				  getNodeStateStr(node->state));
			// Discard the received packets.
			resetPacketQueue(&node->input);

			// The collision corrupted the reception.
			if (node->state == RECEIVING) {
				setNodeState(node, IDLE);
			}
		// If we are receiving the packet, we will have to wait until
		// the transmission is complete. See the RECEIVING case.
		} else if (node->state != RECEIVING) {
			// Process a fresh packet.
			Packet *p = peekPacket(&node->input);
			processPacket(node, p);
			
			// If we move to RECEIVING we keep the packet in the
			// queue for all the receiving period. Otherwise we
			// discard it.
			if (node->state != RECEIVING) {
				dequeuePacket(&node->input);
				releasePacket(p);
			}
		}
	}

	// Generate a new DATA packet.
	if (((double)rand() / RAND_MAX) < sim.params.genProb) {
		if (sim.neighbors[node->id].count > 0) {
			// Select a random neighbor.
			int randIndex = rand() %
				sim.neighbors[node->id].count;
			int dest = sim.neighbors[node->id].data[randIndex];

			Packet *new = createPacket(DATA, node->id, dest);
			sim.packetGenerated++;
			if (enqueuePacket(&node->output, new)) {
				simprintf("Node %d creates DATA Packet %d for "
					  "Node %d\n", node->id, new->id,
					  new->dstId);
			} else {
				simprintf("Node %d queue full, dropped "
					  "generated packet %d\n", node->id,
					  new->id);
				releasePacket(new);
				sim.packetDropped++;
			}
		}
	}

	// Update the 'node' state.
	switch (node->state) {
	case IDLE:
		// Check if we have DATA to send.
		if (!isQueueEmpty(&node->output)) {
			// If channel is idle, wait DIFS.
			if (isChannelIdle(node)) {
				setNodeState(node, WAIT_DIFS);
			}
			// If the channel is busy, we stay IDLE and countinue
			// with carrier sense until it becomes free.
		}
		break;

	case WAIT_DIFS:
		if (!isChannelIdle(node)) {
			// If the channel becomes busy, defer transmission.
			setNodeState(node, IDLE);
		} else {
			node->timer--;
			if (node->timer <= 0) {
				// DIFS complete. Enter backoff.
				setNodeState(node, BACKOFF);
			}
		}
		break;

	case WAIT_SIFS:
		node->timer--;
		if (node->timer <= 0) {
			// Transmit the ACK to neighbors.
			Packet *ack = peekPacket(&node->output);
			assert(ack->type == ACK);

			setNodeState(node, TRANSMITTING);
			node->timer = ack->duration;
		}
		break;

	case BACKOFF: 
		// Backoff timer tiks down only while the channel is free.
		if (isChannelIdle(node)) {
			node->timer -= sim.params.slotTime;
		}
		if (node->timer <= 0) {
			// Backoff complete. Start transmitting.
			Packet *p = peekPacket(&node->output);
			setNodeState(node, TRANSMITTING);
			node->timer = p->duration;
		}
		break;

	case TRANSMITTING: 
		Packet *p = peekPacket(&node->output);

		// Send the packet to neighbors at the start of the trasmitting
		// period.
		if (node->timer == p->duration) {
			simprintf("Node %d starting sending %s Packet %d to "
				  "Node %d\n", node->id,
				  getPacketTypeStr(p->type), p->id,
				  p->dstId);
			sendPacket2Neighbors(p);
		}

		node->timer--;
		if (node->timer <= 0) {
			// Transmission complete.
			switch (p->type) {
			case ACK:	
				simprintf("Node %d finished sending ACK Packet"
					  " %d to Node %d\n", node->id, p->id,
					  p->dstId);

				// Discard the packet.
				dequeuePacket(&node->output);
				releasePacket(p);

				setNodeState(node, IDLE);
				break;

			case DATA:
				simprintf("Node %d finished sending DATA "
					  "Packet %d to Node %d\n", node->id,
					  p->id, p->dstId);

				sim.packetSent++;
				// Do not discard the packet because we may
				// need to resend it if we don't receive an
				// ACK.
				setNodeState(node, WAIT_ACK);
				break;
			}
		}
		break;
	
	case RECEIVING:
		node->timer--;
		if (node->timer <= 0) {
			// Reception complete. Process the packet.
			if (!isQueueEmpty(&node->input)) {
				Packet *p = dequeuePacket(&node->input);
				processPacket(node, p);
				releasePacket(p);
			} else {
				printf("empty queue\n");
				setNodeState(node, IDLE);
			}
		}
		break;

	case WAIT_ACK:
		node->timer--;
		if (node->timer <= 0) {
			// The ACK is not received in time. 
			simprintf("ACK timeout at node %d. Retry to send "
				  "packet\n", node->id);
			sim.collisions++;
			// Increase the contention window.
            if (sim.params.useEpsilonGreedy) {
                rewardNode(node, -50.0);
                selectAction(node);
            } else {
			    increaseCW(node);
            }
			
			// Retry to send DATA packet.
			setNodeState(node, IDLE);
		}
		break;
	}

	//simprintf("Node %d state %s\n", node->id, getNodeStateStr(node->state));
}

/* ==================== Simulation performance metrics  ===================== */

/* Save the value in the 'stat'. The function updates the 'stat' metrics using
 * the incremental formulas of the Welford's algorithm. */
void saveStatValue(Stat *stat, double v) {
	stat->n++;
	double delta = v - stat->mean;
	stat->mean += delta / stat->n;

	double delta2 = v - stat->mean;
	stat->var += delta * delta2;
}

/* Return the average computed by the 'stat'. */
double getStatMean(Stat *stat) {
	return stat->mean;
}

/* Return the standard deviation computed by the 'stat'. */
double getStatStdev(Stat *stat) {
	if (stat->n < 2) return 0.0;
	return sqrt(stat->var / (stat->n - 1));
}

/* Compute the metrics of the simulation. 
 * If this function is called within the same simulation, it updates the stats
 * of the whole simultion: the stats of the simulation becomes the average of
 * its each individual run. */
void calcSimStats(Simulator *sim) {
	if (sim->packetGenerated > 0) {
		double pdr = (double)sim->packetAcked / sim->packetGenerated;
		saveStatValue(&sim->pdr, pdr);
	}

	if (sim->t > 0) {
		double throughput = (double)sim->packetReceived / sim->t;
		saveStatValue(&sim->throughput, throughput);
	}

	if (sim->packetSent > 0) {
		double collRate = (double)sim->collisions / sim->packetSent;
		saveStatValue(&sim->collRate, collRate);
	}

    double runLatency = getStatMean(&sim->avgLatencyRun);
    saveStatValue(&sim->avgLatencyTotal, runLatency);
}

/* Print the simulation metrics based on the number of the simulations run. */
void printSimStats(Simulator *sim, int simRuns) {
	if (simRuns == 1) printf("=========== Simulation Result ===========\n");
	else printf("===== Simulation Results (Average of %d runs) =====\n", simRuns);

    printf("Simulation name:        %s\n", sim->name);

	printf("Time Horizon:		%d\n", sim->params.horizon);
	printf("\n");
	if (simRuns == 1) {
		printf("Packet Generated:	%d\n", sim->packetGenerated);
		printf("Packet Sent:		%d\n", sim->packetSent);
		printf("Packet Received:	%d\n", sim->packetReceived);
		printf("Packet ACKed:		%d\n", sim->packetAcked);
		printf("Packet Dropped:		%d\n", sim->packetDropped);
		printf("\n");
	}
	printf("PDR:			%.4f (%.2f%%)\n",
	       getStatMean(&sim->pdr), getStatMean(&sim->pdr) * 100);
	printf("Throughput:		%.4f pkts/tiks\n", 
	       getStatMean(&sim->throughput));
	printf("Avg Latency:		%.4f tiks\n"
	       "			(Stdev %.4f)\n", 
	       getStatMean(&sim->avgLatencyTotal), 
           getStatStdev(&sim->avgLatencyTotal));
	printf("\n");
	printf("Collision Rate:		%.4f (%.2f%%)\n", 
	       getStatMean(&sim->collRate), getStatMean(&sim->collRate) * 100);

	if (simRuns == 1) printf("=========================================\n");
	else printf("===================================================\n");
}

/* =============================== Simulation =============================== */

/* Set the default value of the configuration parameters. */
void setDefaultConf(SimConf *conf) {
    conf->simRuns        = SIM_RUNS;
	conf->seed           = SEED;
	conf->horizon        = HORIZON;

	conf->dataDur        = DATA_DUR;
	conf->ackDur         = ACK_DUR;
	conf->sifsDur        = SIFS_DUR;
	conf->difsDur        = DIFS_DUR;

	conf->nodeNum        = NODE_NUM;
	conf->range          = RANGE;
	conf->genProb        = GEN_PROB;
	conf->ackTimeout     = ACK_TIMEOUT;

	conf->cwMin          = CW_MIN;
	conf->cwMax          = CW_MAX;
	conf->slotTime       = SLOT_TIME;

	conf->epsilon	     = RL_EPSILON;
	conf->alpha          = RL_ALPHA;

	conf->mareaWidth     = MAREA_WIDTH;
	conf->mareaHeight    = MAREA_HEIGHT;

#ifdef USE_EPSILON_GREEDY
    conf->useEpsilonGreedy = true;
#else
    conf->useEpsilonGreedy = false;
#endif

#ifdef PRINT_MAREA
	conf->printMarea = true;
#else
	conf->printMarea = false;
#endif
#ifdef PRINT_NEIGHBORS
	conf->printNeighbors = true;
#else
	conf->printNeighbors = false;
#endif
#ifdef PRINT_LOG
	conf->printLog = true;
#else
	conf->printLog = false;
#endif
#ifdef PRINT_STATS
	conf->printStats = true;
#else
	conf->printStats = false;
#endif
}

/* Initialize and allocate memory for the simulator. */
void initSimulator(Simulator *sim, SimConf *conf, const char *name) {
	sim->name = strdup(name);
	sim->params = *conf;
	sim->packetId = 0;
	sim->t = 0;

	sim->packetGenerated = 0;
	sim->packetSent      = 0;
	sim->packetReceived  = 0;
	sim->packetAcked     = 0;
	sim->packetDropped   = 0;
	sim->collisions      = 0;

	sim->pdr 	         = (Stat){0};
	sim->throughput      = (Stat){0};
	sim->collRate 	     = (Stat){0};
	sim->avgLatencyRun   = (Stat){0};
	sim->avgLatencyTotal = (Stat){0};
	
	// Set the seed.
	if (conf->seed != -1) srand(conf->seed);

	// Create the nodes.
	sim->nodes = malloc(sizeof(Node*) * conf->nodeNum);
	for (int i = 0; i < conf->nodeNum; i++) {
		sim->nodes[i] = createNode(i);
	}
	
	// Create the monitored area and the neighbors.
	initMarea(&sim->marea, conf->mareaWidth, conf->mareaHeight);
	sim->neighbors = malloc(sizeof(IntArray) * conf->nodeNum);
	// Place the nodes in the monitored area and find the neighbors of each
	// node.
	randPlaceNodes(&sim->marea, sim->nodes, conf->nodeNum);
	buildNeighbors(&sim->marea, sim->neighbors, conf->nodeNum, conf->range);

	if (conf->printMarea) {
		printMarea(&sim->marea, conf->nodeNum-1);
		printf("\n");
	}
	if (conf->printNeighbors) {
		printNeighbors(sim->neighbors, conf->nodeNum);
		printf("\n");
	}
}

/* Reset the simulator state for the next simulation run. 
 * It leaves the stats untouched for update them between simulations. */
void resetSimulator(Simulator *sim) {
	sim->t = 0;
	sim->packetId = 0;
	
	sim->packetGenerated = 0;
	sim->packetSent      = 0;
	sim->packetReceived  = 0;
	sim->packetAcked     = 0;
	sim->packetDropped   = 0;
	sim->collisions      = 0;

	sim->avgLatencyRun = (Stat){0};

    // Do not reset the seed.
	// If you re-seed with time(NULL) inside a fast loop, every run will
    // start with the same seed. Removing this allows the random number
    // generator to continue its sequence.
	//if (sim->params.seed == -1) srand(time(NULL));

	// Reset nodes.
	for (int i = 0; i < sim->params.nodeNum; i++) {
		Node *n = sim->nodes[i];

		freePacketQueue(&n->input);
		freePacketQueue(&n->output);

		n->state = IDLE;
		n->cw = sim->params.cwMin;
		n->timer = 0;

        for (int k = 0; k < ARMS_NUM; k++) n->qValues[k] = 0.0;
        n->lastAction = 0;
	}

	// Clear the mearea and neighbors.
	freeNeighbors(sim->neighbors, sim->params.nodeNum);
    freeMarea(&sim->marea);
	// Clear the monitored area.
	initMarea(&sim->marea, sim->params.mareaWidth,
		  sim->params.mareaHeight);

	// Re-place nodes and rebuild neighbors.
	randPlaceNodes(&sim->marea, sim->nodes, sim->params.nodeNum);
	buildNeighbors(&sim->marea, sim->neighbors, sim->params.nodeNum,
			sim->params.range);
}

/* Free the memory allocated for the simulator. */
void freeSimulator(Simulator *sim) {
	for (int i = 0; i < sim->params.nodeNum; i++) {
		freePacketQueue(&sim->nodes[i]->input);
		freePacketQueue(&sim->nodes[i]->output);
		free(sim->nodes[i]);
	}
	free(sim->name);
	free(sim->nodes);
	freeNeighbors(sim->neighbors, sim->params.nodeNum);
	free(sim->neighbors);
	freeMarea(&sim->marea);
}

/* Print the formatted string with the simulation time. */
void simprintf(const char *fmt, ...) {
	va_list ap;
	va_start(ap, fmt);

	if (sim.params.printLog) {
		printf("[%.2d:%.2d:%.2d] ", (sim.t/10000), (sim.t/100)%100,
		       sim.t%100);
		vprintf(fmt, ap);
	}

	va_end(ap);
}

/* Run a simulation. */
void runSimulation(Simulator *sim) {
	// Simulation loop.
	for (sim->t = 0; sim->t <= sim->params.horizon; sim->t++) {
		for (int i = 0; i < sim->params.nodeNum; i++) {
			updateNode(sim->nodes[i]);
		}

#ifdef PRINT_SIM_INFO
        // Print the progress bar.
        if (sim->t == 0) { printf("["); fflush(stdout); }
        int n = 10;
        if ((sim->t % (sim->params.horizon / (n-1))) == 0) {
            printf("*");
            fflush(stdout);
        }
        if (sim->t == sim->params.horizon) printf("]\n");
#endif
	}
}

/* Run multiple simulations with specified names and configurations. 
 * This function accepts multiple simulation scenarios in a single call. We
 * should provide 'count' pairs of simulation name and simulation configuration
 * of types char* and SimConf* respectively. 
 * The function iterates through the provided arguments, running the simulation
 * for each configuration provided. 
 *
 * Example of usage:
 * runSimulationsWithConfs(2, "High traffic", confHigh, "Low Traffic", confLow);
 * */
void runSimulationsWithConfs(int count, ...) {
    va_list args;
    va_start(args, count);

    for (int i = 0; i < count; i++) {
        char *name = va_arg(args, char*);
        SimConf *conf = va_arg(args, SimConf*);

        initSimulator(&sim, conf, name);

        for (int r = 0; r < sim.params.simRuns; r++) {
            resetSimulator(&sim);
#ifdef PRINT_SIM_INFO
            printf("%s simulation run %d/%d ", sim.name, r+1, sim.params.simRuns);
#endif

            runSimulation(&sim);	
            calcSimStats(&sim);
        }

        if (sim.params.printStats) {
            printf("\n");
            printSimStats(&sim, sim.params.simRuns);
        }

        freeSimulator(&sim);
        printf("\n");
    }

    va_end(args);
}

/* ========================= Reinforcement learning ========================= */
/* The following functions implement a Reinforcement Learning (RL) strategy 
 * using the K-Armed Bandit framework to adaptively select the contention 
 * window size. 
 *
 * Each "arm" corresponds to a specific CW size (e.g., 15, 31, ..., 1023). The
 * node acts as an agent trying to maximize its expected reward.
 *
 * The node chooses an action based on 
 * - Exploit: with probability (1 - epsilon), it selects the arm with the 
 *   highest estimated value (greedy selection).
 * - Explore: with probability epsilon, it selects a random arm.
 * The Q-values are estimates of the expected reward for each CW size.
 *
 * After a transmission, the node receives a reward and updates the Q-value of
 * the chosen arm using an alpha step-sizea. This allows the estimates to adapt
 * to recent changes by weighting new rewards more heavily than old ones. */

/* Convert an arm index to a Contention Window (CW) size.
 * Arm 0 -> CW 15   (2^4 - 1)
 * Arm 1 -> CW 31   (2^5 - 1)
 * ...
 * Arm 6 -> CW 1023 (2^10 - 1) */
int getCWFromArm(int armIndex) {
	return (1 << (4 + armIndex)) - 1;
}

/* Select the next contention window using the Epsilon-Greedy strategy. */
void selectAction(Node *node) {
	int action = 0;

	// Explore. Choose a random arm.
	if (((double)rand() / RAND_MAX) < sim.params.epsilon) {
		action = rand() % ARMS_NUM;
	}
	// Eploit. Choose the arm with the highest Q-value.
	else {
		int bestArm = 0;
		double maxQ = node->qValues[0];

		for (int i = 0; i < ARMS_NUM; i++) {
			if (node->qValues[i] > maxQ) {
				maxQ = node->qValues[i];
				bestArm = i;
			}
		}
		action = bestArm;
	}

	// Apply the action.
	node->lastAction = action;
	node->cw = getCWFromArm(action);

	if (node->cw < sim.params.cwMin) node->cw = sim.params.cwMin;
	if (node->cw > sim.params.cwMax) node->cw = sim.params.cwMax;
}

/* Update the Q-value for the last taken action based on the reward received.
 * Uses the constant step-size alpha:
 * 	 Q(A) <- Q(A) + alpha * [Reward - Q(A)] 
 */
void rewardNode(Node *node, double reward) {
	int a = node->lastAction;
	double oldQ = node->qValues[a];

	node->qValues[a] = oldQ + sim.params.alpha * (reward - oldQ);

	simprintf("RL update Node %d: Action CW = %d, Reward = %.1f, NewQ = %.4f\n", node->id, getCWFromArm(a), reward, node->qValues[a]);
}

/* ================================== Plot ================================== */

/* Initialize and allocate memory for the plot. */
void initPlot(Plot *p, char *title, char *xlabel, char *ylabel) {
    p->title    = strdup(title);
    p->xlabel   = strdup(xlabel);
    p->ylabel   = strdup(ylabel);

    p->seriesCap = 5;
    p->seriesCount = 0;
    p->series = malloc(sizeof(PlotSeries) * p->seriesCap);
}

/* Add a data series to the plot. Returns the index of the series. */
int addPlotSeries(Plot *p, char *label) {
    if (p->seriesCount >= p->seriesCap) {
        p->seriesCap *= 2;
        p->series = realloc(p->series, sizeof(PlotSeries) * p->seriesCap);
    }

    PlotSeries s;
    s.label = strdup(label);
    s.count = 0;
    s.capacity = 128;
    s.points = malloc(sizeof(Point) * s.capacity);

    p->series[p->seriesCount] = s;

    return p->seriesCount++;
}

/* Add a data point to a series. */
void addPlotData(Plot *p, int seriesIndex, double x, double y) {
    if (seriesIndex < 0 || seriesIndex >= p->seriesCount) return;

    PlotSeries *s = &p->series[seriesIndex];

    if (s->count >= s->capacity) {
        s->capacity *= 2;
        s->points = realloc(s->points, sizeof(Point) * s->capacity);
    }

    Point pt = (Point){.x = x, .y = y};
    s->points[s->count] = pt;
    s->count++;
}

/* Execute gnuplot to generate the plot image. */
void generatePlot(Plot *p, const char *filename) {
    FILE *gp = popen("gnuplot -persistent", "w");
    if (gp == NULL) {
        fprintf(stderr, "Error opening pipe to GNUplot.\n");
        return;
    }

    // Gnuplot configuration.
    fprintf(gp, "set terminal pngcairo size 800,600 enhanced font 'Verdana,10'\n");
    fprintf(gp, "set output '%s'\n", filename);
    fprintf(gp, "set title '%s'\n", p->title);
    fprintf(gp, "set xlabel '%s'\n", p->xlabel);
    fprintf(gp, "set ylabel '%s'\n", p->ylabel);
    fprintf(gp, "set grid\n");
    fprintf(gp, "set key outside right top\n"); // Legend placement.

    // Construct the plot command.
    fprintf(gp, "plot ");
    for (int i = 0; i < p->seriesCount; i++) {
        fprintf(gp, "'-' with lines lw 2 title '%s'", p->series[i].label);
        if (i < p->seriesCount - 1) fprintf(gp, ", ");
    }
    fprintf(gp, "\n");

    // Send data for each series.
    for (int i = 0; i < p->seriesCount; i++) {
        PlotSeries *s = &p->series[i];
        for (int k = 0; k < s->count; k++) {
            fprintf(gp, "%f %f\n", s->points[k].x, s->points[k].y);
        }
        fprintf(gp, "e\n"); // End of data marker for gnuplot.
    }

    pclose(gp);
    printf("Plot generated: %s\n", filename);
}

/* Free the memory allocated for the plot. */
void freePlot(Plot *p) {
    if (p == NULL) return;
    free(p->title);
    free(p->xlabel);
    free(p->ylabel);

    for (int i = 0; i < p->seriesCount; i++) {
        free(p->series[i].label);
        free(p->series[i].points);
    }

    free(p->series);
}

/* ================================== Main ================================== */

int main(void) {
    if (SEED == -1) srand(time(NULL));

    SimConf baseConf;
    setDefaultConf(&baseConf);

#if 0
    // Standard CSMA/CA vs Adaptive CW.
    SimConf standardConf = baseConf;
    standardConf.useEpsilonGreedy = false;
    
    SimConf adaptiveConf = baseConf;
    adaptiveConf.useEpsilonGreedy = true;
 
    runSimulationsWithConfs(2,
        "Standard",    &standardConf,
        "Adaptive CW", &adaptiveConf
    );
#endif

#if 0
    // Node density.
    SimConf lowDensityConf = baseConf;
    lowDensityConf.useEpsilonGreedy = false;
    lowDensityConf.nodeNum = 5;

    SimConf medDensityConf = baseConf;
    medDensityConf.useEpsilonGreedy = false;
    medDensityConf.nodeNum = 10;

    SimConf higDensityConf = baseConf;
    higDensityConf.useEpsilonGreedy = false;
    higDensityConf.nodeNum = 15;
    
    runSimulationsWithConfs(3,
        "Standard CSMA/CA low node density (5 nodes)",     &lowDensityConf,
        "Standard CSMA/CA medium node density (10 nodes)", &medDensityConf,
        "Standard CSMA/CA high node density (15 nodes)",   &higDensityConf
    );

    lowDensityConf.useEpsilonGreedy = true;
    medDensityConf.useEpsilonGreedy = true;
    higDensityConf.useEpsilonGreedy = true;

    runSimulationsWithConfs(3,
        "Adaptive CW low node density (5 nodes)",     &lowDensityConf,
        "Adaptive CW medium node density (10 nodes)", &medDensityConf,
        "Adaptive CW high node density (15 nodes)",   &higDensityConf
    );
#endif

#if 0
    // Network traffic.
    SimConf lowTrafficConf = baseConf;
    lowTrafficConf.useEpsilonGreedy = false;
    lowTrafficConf.genProb = 0.003;

    SimConf medTrafficConf = baseConf;
    medTrafficConf.useEpsilonGreedy = false;
    medTrafficConf.genProb = 0.005;

    SimConf higTrafficConf = baseConf;
    higTrafficConf.useEpsilonGreedy = false;
    higTrafficConf.genProb = 0.010;

    runSimulationsWithConfs(3,
        "Standard CSMA/CA low traffic (0.003 genProb)",    &lowTrafficConf,
        "Standard CSMA/CA medium traffic (0.005 genProb)", &medTrafficConf,
        "Standard CSMA/CA high traffic (0.010 genProb)",   &higTrafficConf
    );

    lowTrafficConf.useEpsilonGreedy = true;
    medTrafficConf.useEpsilonGreedy = true;
    higTrafficConf.useEpsilonGreedy = true;

    runSimulationsWithConfs(3,
        "Adaptive CW low traffic (0.003 genProb)",    &lowTrafficConf,
        "Adaptive CW medium traffic (0.005 genProb)", &medTrafficConf,
        "Adaptive CW high traffic (0.010 genProb)",   &higTrafficConf
    );
#endif

#if 0
    // Exploration rate.
    SimConf lowExplorationConf = baseConf;
    lowExplorationConf.useEpsilonGreedy = true;
    lowExplorationConf.epsilon = 0.001;

    SimConf medExplorationConf = baseConf;
    medExplorationConf.useEpsilonGreedy = true;
    medExplorationConf.epsilon = 0.010;

    SimConf higExplorationConf = baseConf;
    higExplorationConf.useEpsilonGreedy = true;
    higExplorationConf.epsilon = 0.030;

    runSimulationsWithConfs(3,
        "Adaptive CW low exploration rate (0.001 epsilon)",    &lowExplorationConf,
        "Adaptive CW medium exploration rate (0.010 epsilon)", &medExplorationConf,
        "Adaptive CW high exploration rate (0.030 epsilon)",   &higExplorationConf
    );
#endif

#if 0
    // Learning rate.
    SimConf lowLearningConf = baseConf;
    lowLearningConf.useEpsilonGreedy = true;
    lowLearningConf.alpha = 0.10;

    SimConf medLearningConf = baseConf;
    medLearningConf.useEpsilonGreedy = true;
    medLearningConf.alpha = 0.20;

    SimConf higLearningConf = baseConf;
    higLearningConf.useEpsilonGreedy = true;
    higLearningConf.alpha = 0.30;

    runSimulationsWithConfs(3,
        "Adaptive CW low learning rate (0.10 alpha)",    &lowLearningConf,
        "Adaptive CW medium learning rate (0.20 alpha)", &medLearningConf,
        "Adaptive CW high learning rate (0.30 alpha)",   &higLearningConf
    );
#endif

    return 0;
}

