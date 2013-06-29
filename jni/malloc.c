#include <stdlib.h>
#include <jni.h>
#include <android/log.h>

typedef struct mem_block {
	struct mem_block *pNext;
} MEM_BLK;

static MEM_BLK *pTop = NULL, *pButtom = NULL;

int Java_jp_saka_heap_Heap_alloc(JNIEnv* env, jobject thiz, jint size)
{
	MEM_BLK *pMemBlock;

	if (size <= 0) {
		return 0;
	}
	
	size = (size < sizeof(MEM_BLK)) ? sizeof(MEM_BLK) : size;
	pMemBlock = (MEM_BLK *)malloc(size);
	if (pMemBlock == NULL) {
		return 0;
	}

	memset(pMemBlock, 0x55, size);

	pMemBlock->pNext = NULL;

	if (pTop == NULL) {
		pTop = pButtom = pMemBlock;
	} else {
		pButtom->pNext = pMemBlock;
		pButtom = pMemBlock;
	}

	return size;
}

void Java_jp_saka_heap_Heap_free(JNIEnv* env, jobject thiz)
{
	MEM_BLK *pMemBlock = pTop, *pMemBlockNext;
	while (pMemBlock != NULL) {
		pMemBlockNext = pMemBlock->pNext;
		free(pMemBlock);
		pMemBlock = pMemBlockNext;
	}

	pTop = pButtom = NULL;
}

