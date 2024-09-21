import java.util.*;
import java.io.*;

/*
문제풀이 아이디어
: 각 턴을 k번 반복
: 각 턴의 점수 총합 score
: 유적벽면의 조각을 가리키는 포인터 pointer

1. 회전시킬 수 있는 모든 경우에 대하여 회전 후 유물의 가치 확인
    1-1. 가치가 최대인 경우의 회전중심좌표와 회전각도 저장
    1-2. 앞서 저장한 경우와 가치가 같다면 갱신x
    1-3. 탐색순서: 90도 (1,1)(2,1),(3,1)(1,2)(2,2)(3,2)(1,3)(2,3)(3,3) -> 180도 ...
- 3*3을 90,180,270도 회전시키는 함수
- 5*5맵에서 유물의 가치를 세는 함수

2. 유물 제거 후, 제거한 유물의 가치 반환
    2-1. mapCopy생성
    2-2. 5*5 맵을 돌면서 상하좌우 bfs탐색 수행 => 이때 스타팅위치마다 각기 다른 번호를 부여. 해당 번호로 mapCopy에 표시
    2-3. mapCopy를 돌면서 개수가 3개 이상인 번호들을 따로 저장
    2-4. mapCopy를 돌면서 따로 저장한 번호를 만나면, 해당 위치의 map에 -1 기록 후 cnt+=1

3. 새로운 조각 채우기
    3-1. 작은열>큰열, 큰행>작은행으로 탐색하며 -1을 만날 땐 pointer가 가리키는 조각을 넣기

4. 없앤 유물의 개수가 0일 때까지 2와3을 반복
5. score값 출력
*/

public class Main {
    static BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    static StringBuilder sb = new StringBuilder();
    static StringTokenizer st;

    static int k, m;
    static int[][] map, mapCopy;
    static int[] wall;
    static int pointer;
    static int turnRow, turnCol, turnCnt;
    static Deque<Integer> deque;

    static int[] dr = {-1, 1, 0, 0};
    static int[] dc = {0, 0, -1, 1};

    static HashMap<Integer, Integer> dict;




    public static void main(String[] args) throws IOException {
        //입력
        inputTestcase();
        //각 턴을 k번 반복
        for(int idx=0; idx<k; idx++){
            int score=0;
            //회전시키기
            best_turn_matrix();
            //유물 연쇄 획득
            while(true){
                //유물 제거 후, 제거한 유물의 가치 반환
                int price=SearchRemove();
                if(price==0){break;}
                score+=price;
                //새로운 조각 채우기
                add();
            }
            //반복이 종료되면, score값 출력
            if(score==0){break;}
            sb.append(score).append(" ");
        }
        //출력
        System.out.println(sb);
    }

    static void inputTestcase() throws IOException {
        st = new StringTokenizer(br.readLine().trim());
        k = Integer.parseInt(st.nextToken());
        m = Integer.parseInt(st.nextToken());

        map = new int[5][5];
        for(int row=0; row<5; row++){
            st = new StringTokenizer(br.readLine().trim());
            for(int col=0; col<5; col++){
                map[row][col]=Integer.parseInt(st.nextToken());
            }
        }

        wall = new int[m];
        st = new StringTokenizer(br.readLine().trim());
        for(int idx=0; idx<m; idx++){
            wall[idx] = Integer.parseInt(st.nextToken());
        }
    }

    static void turn(int row, int col){ //시계방향으로 90도 회전
        int[][] beforeTurn = new int[3][3];
        int[][] afterTurn = new int[3][3];
        for(int nowR=row-1; nowR<=row+1; nowR++){
            for(int nowC=col-1; nowC<=col+1; nowC++){
                beforeTurn[nowR-(row-1)][nowC-(col-1)]=map[nowR][nowC];
            }
        }
        for(int nowR=0; nowR<3; nowR++){
            for(int nowC=0; nowC<3; nowC++){
                afterTurn[nowR][nowC]=beforeTurn[3-1-nowC][nowR];
            }
        }
        for(int nowR=row-1; nowR<=row+1; nowR++){
            for(int nowC=col-1; nowC<=col+1; nowC++){
                map[nowR][nowC]=afterTurn[nowR-(row-1)][nowC-(col-1)];
            }
        }
    }

    static void turnReverse(int row, int col){ //반시계방향으로 90도 회전
        int[][] beforeTurn = new int[3][3];
        int[][] afterTurn = new int[3][3];
        for(int nowR=row-1; nowR<=row+1; nowR++){
            for(int nowC=col-1; nowC<=col+1; nowC++){
                beforeTurn[nowR-(row-1)][nowC-(col-1)]=map[nowR][nowC];
            }
        }
        for(int nowR=0; nowR<3; nowR++){
            for(int nowC=0; nowC<3; nowC++){
                afterTurn[nowR][nowC]=beforeTurn[nowC][3-1-nowR];
            }
        }
        for(int nowR=row-1; nowR<=row+1; nowR++){
            for(int nowC=col-1; nowC<=col+1; nowC++){
                map[nowR][nowC]=afterTurn[nowR-(row-1)][nowC-(col-1)];
            }
        }
    }

    static int countValue(){
        int cnt=0;
        dict = new HashMap<>();
        mapCopy = new int[5][5];
        int num=0;
        for(int row=0; row<5; row++){
            for(int col=0; col<5; col++){
                if(mapCopy[row][col]!=0){continue;}
                num++;
                bfs(row, col, num);
            }
        }
        //mapCopy를 돌면서 개수가 3개 이상인 번호들을 따로 저장
        for(int row=0; row<5; row++){
            for(int col=0; col<5; col++){
                //dict.put: mapCopy[row][col]를 key로 갖는 value값을 재설정
                //dict.getOrDefault: mapCopy[row][col]가 key로 있으면 value반환 없으면 0반환
                dict.put(mapCopy[row][col],dict.getOrDefault(mapCopy[row][col], 0)+1);
            }
        }
        //mapCopy를 돌면서 따로 저장한 번호를 만나면, 해당 위치의 map에 -1 기록 후 cnt+=1
        for(int row=0; row<5; row++){
            for(int col=0; col<5; col++){
                if(dict.get(mapCopy[row][col])>=3){
                    cnt+=1;
                }
            }
        }
        return cnt;
    }

    static void best_turn_matrix(){
        int row=0;
        int col=0;
        int cnt=0;
        int result=0;

        for(int tCnt=1; tCnt<=3; tCnt++){ //회전각도
            for(int tCol=1; tCol<=3; tCol++){ //중심좌표의 열
                for(int tRow=1; tRow<=3; tRow++){ //중심좌표의 행
                    //회전시키기
                    for(int idx=0; idx<tCnt; idx++){
                        turn(tRow, tCol); //시계방향으로 90도 회전
                    }
                    //유물가치 세기
                    int nowResult = countValue();
                    if(nowResult>result){
                        row = tRow;
                        col = tCol;
                        cnt = tCnt;
                        result=nowResult;
                    }
                    //다시 원래대로
                    for(int idx=0; idx<tCnt; idx++){
                        turnReverse(tRow, tCol); //반시계방향으로 90도 회전
                    }
                }
            }
        }

        //회전시키기
        for(int idx=0; idx<cnt; idx++){
            turn(row, col); //시계방향으로 90도 회전
        }
    }

    //map에서 (row,col)좌표를 시작으로 상하좌우 같은 숫자 찾기
    static void bfs(int row, int col, int num){
        int mapVal = map[row][col];
        deque = new ArrayDeque<>();
        //시작좌표 넣기
        deque.addLast(row);
        deque.addLast(col);
        //mapCopy에 시작점 표시
        mapCopy[row][col]=num;

        while(!deque.isEmpty()){
            int nowRow = deque.removeFirst();
            int nowCol = deque.removeFirst();
            for(int idx=0; idx<4; idx++){
                int nextRow = nowRow+dr[idx];
                int nextCol = nowCol+dc[idx];
                //다음 위치가 맵범위를 벗어난다면
                if(nextRow<0 || nextCol<0 || nextRow>=5 || nextCol>=5){
                    continue;
                }
                //다음 위치가 이미 방문한 칸이라면
                if(mapCopy[nextRow][nextCol]!=0){
                    continue;
                }
                //다음 위치가 현재 mapVal와 동일하다면
                if(map[nextRow][nextCol]==mapVal){
                    deque.addLast(nextRow);
                    deque.addLast(nextCol);
                    mapCopy[nextRow][nextCol]=num;
                }
            }
        }
    }

    static int SearchRemove(){
        //3개 이상 연결되었는지 찾기
        int cnt=0;
        dict = new HashMap<>();
        mapCopy = new int[5][5];
        int num=0;
        for(int row=0; row<5; row++){
            for(int col=0; col<5; col++){
                if(mapCopy[row][col]!=0){continue;}
                num++;
                bfs(row, col, num);
            }
        }
        //mapCopy를 돌면서 개수가 3개 이상인 번호들을 따로 저장
        for(int row=0; row<5; row++){
            for(int col=0; col<5; col++){
                //dict.put: mapCopy[row][col]를 key로 갖는 value값을 재설정
                //dict.getOrDefault: mapCopy[row][col]가 key로 있으면 value반환 없으면 0반환
                dict.put(mapCopy[row][col],dict.getOrDefault(mapCopy[row][col], 0)+1);
            }
        }
        //mapCopy를 돌면서 따로 저장한 번호를 만나면, 해당 위치의 map에 -1 기록 후 cnt+=1
        for(int row=0; row<5; row++){
            for(int col=0; col<5; col++){
                if(dict.get(mapCopy[row][col])>=3){
                    map[row][col]=-1;
                    cnt+=1;
                }
            }
        }
        return cnt;
    }

    static void add(){
        for(int col=0; col<5; col++){
            for(int row=4; row>=0; row--){
                if(map[row][col]==-1){
                    map[row][col]=wall[pointer];
                    pointer++;
                }
            }
        }
    }


}